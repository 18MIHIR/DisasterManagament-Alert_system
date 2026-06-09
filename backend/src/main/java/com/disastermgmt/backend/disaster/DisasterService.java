package com.disastermgmt.backend.disaster;

import com.disastermgmt.backend.alert.AlertService;
import com.disastermgmt.backend.user.User;
import com.disastermgmt.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DisasterService {

    private final DisasterRepository disasterRepository;
    private final AlertService alertService;
    private final UserRepository userRepository;

    public DisasterService(DisasterRepository disasterRepository,
                           AlertService alertService,
                           UserRepository userRepository) {
        this.disasterRepository = disasterRepository;
        this.alertService = alertService;
        this.userRepository = userRepository;
    }

    public List<DisasterDTO> getAllDisasters() {
        return disasterRepository.findAllByCountryIgnoreCase("India").stream()
                .map(DisasterDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<DisasterDTO> getDisasterById(Long id) {
        return disasterRepository.findById(id)
                .filter(d -> "India".equalsIgnoreCase(d.getCountry()))
                .map(DisasterDTO::fromEntity);
    }

    public List<DisasterDTO> getActiveDisasters() {
        return disasterRepository.findActiveDisastersOrderedBySeverity().stream()
                .map(DisasterDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DisasterDTO> getPendingDisasters() {
        return disasterRepository.findPendingDisasters().stream()
                .map(DisasterDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DisasterDTO> getDisastersWithFilters(DisasterType type, DisasterSeverity severity,
                                                      DisasterStatus status, String region) {
        return disasterRepository.findWithFilters(type, severity, status, region).stream()
                .map(DisasterDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DisasterDTO> getRecentDisasters(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return disasterRepository.findRecentDisasters(startTime).stream()
                .map(DisasterDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DisasterDTO createDisaster(CreateDisasterRequest request, Long reportedByUserId) {
        Disaster disaster = new Disaster();
        disaster.setTitle(request.getTitle());
        disaster.setDescription(request.getDescription());
        disaster.setType(request.getType());
        disaster.setSeverity(request.getSeverity());
        disaster.setStatus(DisasterStatus.PENDING);
        disaster.setLocation(request.getLocation());
        disaster.setRegion(request.getRegion());
        disaster.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        disaster.setLatitude(request.getLatitude());
        disaster.setLongitude(request.getLongitude());
        disaster.setEventTime(request.getEventTime() != null ? request.getEventTime() : LocalDateTime.now());
        disaster.setAdvisoryMessage(request.getAdvisoryMessage());
        disaster.setAffectedAreas(request.getAffectedAreas());
        disaster.setEstimatedAffectedPopulation(request.getEstimatedAffectedPopulation());

        if (reportedByUserId != null) {
            disaster.setSource("RESPONDER_REPORT");
            disaster.setReportedBy(reportedByUserId);
        } else {
            disaster.setSource("ADMIN_ENTRY");
        }

        Disaster saved = disasterRepository.save(disaster);
        return DisasterDTO.fromEntity(saved);
    }

    @Transactional
    public DisasterDTO createFromExternalSource(Disaster disaster) {
        Optional<Disaster> existing = disasterRepository
                .findByExternalIdAndSource(disaster.getExternalId(), disaster.getSource());
        
        if (existing.isPresent()) {
            Disaster existingDisaster = existing.get();
            existingDisaster.setTitle(disaster.getTitle());
            existingDisaster.setDescription(disaster.getDescription());
            existingDisaster.setSeverity(disaster.getSeverity());
            existingDisaster.setLatitude(disaster.getLatitude());
            existingDisaster.setLongitude(disaster.getLongitude());
            existingDisaster.setAdvisoryMessage(disaster.getAdvisoryMessage());
            return DisasterDTO.fromEntity(disasterRepository.save(existingDisaster));
        }

        disaster.setStatus(DisasterStatus.PENDING);
        Disaster saved = disasterRepository.save(disaster);
        return DisasterDTO.fromEntity(saved);
    }

    @Transactional
    public Optional<DisasterDTO> updateDisaster(Long id, UpdateDisasterRequest request) {
        return disasterRepository.findById(id).map(disaster -> {
            if (request.getTitle() != null) disaster.setTitle(request.getTitle());
            if (request.getDescription() != null) disaster.setDescription(request.getDescription());
            if (request.getType() != null) disaster.setType(request.getType());
            if (request.getSeverity() != null) disaster.setSeverity(request.getSeverity());
            if (request.getStatus() != null) {
                disaster.setStatus(request.getStatus());
                if (request.getStatus() == DisasterStatus.RESOLVED) {
                    disaster.setResolvedAt(LocalDateTime.now());
                }
            }
            if (request.getLocation() != null) disaster.setLocation(request.getLocation());
            if (request.getRegion() != null) disaster.setRegion(request.getRegion());
            if (request.getCountry() != null) disaster.setCountry(request.getCountry());
            if (request.getLatitude() != null) disaster.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) disaster.setLongitude(request.getLongitude());
            if (request.getEventTime() != null) disaster.setEventTime(request.getEventTime());
            if (request.getAdvisoryMessage() != null) disaster.setAdvisoryMessage(request.getAdvisoryMessage());
            if (request.getAffectedAreas() != null) disaster.setAffectedAreas(request.getAffectedAreas());
            if (request.getEstimatedAffectedPopulation() != null) {
                disaster.setEstimatedAffectedPopulation(request.getEstimatedAffectedPopulation());
            }

            return DisasterDTO.fromEntity(disasterRepository.save(disaster));
        });
    }

    @Transactional
    public Optional<DisasterDTO> verifyAndPushAlert(Long id, Long adminUserId) {
        User admin = userRepository.findById(adminUserId).orElse(null);
        return disasterRepository.findById(id).map(disaster -> {
            disaster.setStatus(DisasterStatus.ACTIVE);
            disaster.setVerifiedBy(adminUserId);
            disaster.setAlertBroadcastAt(LocalDateTime.now());
            Disaster saved = disasterRepository.save(disaster);
            if (admin != null) {
                String message = disaster.getAdvisoryMessage() != null && !disaster.getAdvisoryMessage().isBlank()
                        ? disaster.getAdvisoryMessage()
                        : null;
                alertService.createAlert(saved.getId(), message, admin);
            }
            return DisasterDTO.fromEntity(saved);
        });
    }

    @Transactional
    public Optional<DisasterDTO> resolveDisaster(Long id) {
        return disasterRepository.findById(id).map(disaster -> {
            disaster.setStatus(DisasterStatus.RESOLVED);
            disaster.setResolvedAt(LocalDateTime.now());
            return DisasterDTO.fromEntity(disasterRepository.save(disaster));
        });
    }

    @Transactional
    public Optional<DisasterDTO> cancelDisaster(Long id) {
        return disasterRepository.findById(id).map(disaster -> {
            disaster.setStatus(DisasterStatus.CANCELLED);
            return DisasterDTO.fromEntity(disasterRepository.save(disaster));
        });
    }

    @Transactional
    public void deleteDisaster(Long id) {
        disasterRepository.deleteById(id);
    }

    @Transactional
    public int deleteAllPendingDisasters() {
        return disasterRepository.deleteByStatus(DisasterStatus.PENDING);
    }

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalActive(disasterRepository.countByStatus(DisasterStatus.ACTIVE));
        stats.setTotalPending(disasterRepository.countByStatus(DisasterStatus.PENDING));
        stats.setTotalResolved(disasterRepository.countByStatus(DisasterStatus.RESOLVED));
        stats.setActiveFloods(disasterRepository.countActiveByType(DisasterType.FLOOD));
        stats.setActiveEarthquakes(disasterRepository.countActiveByType(DisasterType.EARTHQUAKE));
        stats.setActiveCyclones(disasterRepository.countActiveByType(DisasterType.CYCLONE));
        stats.setActiveWildfires(disasterRepository.countActiveByType(DisasterType.WILDFIRE));
        return stats;
    }

    public static class DashboardStats {
        private Long totalActive;
        private Long totalPending;
        private Long totalResolved;
        private Long activeFloods;
        private Long activeEarthquakes;
        private Long activeCyclones;
        private Long activeWildfires;

        public Long getTotalActive() { return totalActive; }
        public void setTotalActive(Long totalActive) { this.totalActive = totalActive; }
        public Long getTotalPending() { return totalPending; }
        public void setTotalPending(Long totalPending) { this.totalPending = totalPending; }
        public Long getTotalResolved() { return totalResolved; }
        public void setTotalResolved(Long totalResolved) { this.totalResolved = totalResolved; }
        public Long getActiveFloods() { return activeFloods; }
        public void setActiveFloods(Long activeFloods) { this.activeFloods = activeFloods; }
        public Long getActiveEarthquakes() { return activeEarthquakes; }
        public void setActiveEarthquakes(Long activeEarthquakes) { this.activeEarthquakes = activeEarthquakes; }
        public Long getActiveCyclones() { return activeCyclones; }
        public void setActiveCyclones(Long activeCyclones) { this.activeCyclones = activeCyclones; }
        public Long getActiveWildfires() { return activeWildfires; }
        public void setActiveWildfires(Long activeWildfires) { this.activeWildfires = activeWildfires; }
    }
}
