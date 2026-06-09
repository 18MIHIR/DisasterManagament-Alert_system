package com.disastermgmt.backend.alert;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.disaster.DisasterRepository;
import com.disastermgmt.backend.user.User;
import com.disastermgmt.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;

    public AlertService(AlertRepository alertRepository,
                        DisasterRepository disasterRepository,
                        UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
    }

    public List<AlertDTO> getAlertsForCitizenRegion(String region) {
        List<Alert> alerts = alertRepository.findAllByOrderByBroadcastTimeDesc();
        if (region == null || region.isBlank()) {
            return alerts.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }

        String normalized = region.trim().toLowerCase();
        List<AlertDTO> matched = alerts.stream()
                .filter(alert -> {
                    String alertRegion = alert.getRegion() != null ? alert.getRegion().trim().toLowerCase() : "";
                    return alertRegion.contains(normalized)
                            || normalized.contains(alertRegion)
                            || alertRegion.contains("india")
                            || normalized.contains("india");
                })
                .map(this::toDTO)
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            return matched;
        }

        // If no exact regional match exists, return all nationwide alerts so citizens still receive live notification.
        return alerts.stream()
                .filter(alert -> alert.getRegion() != null && alert.getRegion().toLowerCase().contains("india"))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AlertDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .sorted((a, b) -> b.getBroadcastTime().compareTo(a.getBroadcastTime()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Alert createAlert(Long disasterId, String message, User createdBy) {
        Disaster disaster = disasterRepository.findById(disasterId).orElse(null);
        if (disaster == null) return null;

        String region = disaster.getRegion() != null && !disaster.getRegion().isBlank()
                ? disaster.getRegion().trim()
                : disaster.getLocation();

        Alert alert = new Alert();
        alert.setDisaster(disaster);
        alert.setMessage(message != null && !message.isBlank() ? message : buildDefaultMessage(disaster));
        alert.setCreatedBy(createdBy);
        alert.setBroadcastTime(java.time.LocalDateTime.now());
        alert.setRegion(region);
        return alertRepository.save(alert);
    }

    private String buildDefaultMessage(Disaster d) {
        return String.format("%s - %s in %s. Severity: %s. %s",
                d.getTitle(), d.getType(), d.getLocation(), d.getSeverity(),
                d.getAdvisoryMessage() != null ? d.getAdvisoryMessage() : "Please follow local advisories.");
    }

    private AlertDTO toDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setDisasterId(alert.getDisaster().getId());
        dto.setDisasterTitle(alert.getDisaster().getTitle());
        dto.setMessage(alert.getMessage());
        dto.setCreatedById(alert.getCreatedBy().getId());
        dto.setCreatedByName(alert.getCreatedBy().getName());
        dto.setBroadcastTime(alert.getBroadcastTime());
        dto.setRegion(alert.getRegion());
        return dto;
    }
}
