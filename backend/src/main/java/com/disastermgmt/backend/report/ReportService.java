package com.disastermgmt.backend.report;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.disaster.DisasterRepository;
import com.disastermgmt.backend.rescuetask.RescueTask;
import com.disastermgmt.backend.rescuetask.RescueTaskRepository;
import com.disastermgmt.backend.user.User;
import com.disastermgmt.backend.user.UserRepository;
import com.disastermgmt.backend.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.disastermgmt.backend.disaster.DisasterType;
import com.disastermgmt.backend.disaster.DisasterSeverity;
import com.disastermgmt.backend.disaster.DisasterStatus;

@Service
public class ReportService {

    private static final int MAX_IMAGE_DATA_CHARS = 750_000;

    private final ReportRepository reportRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;
    private final RescueTaskRepository rescueTaskRepository;

    public ReportService(ReportRepository reportRepository,
                         DisasterRepository disasterRepository,
                         UserRepository userRepository,
                         RescueTaskRepository rescueTaskRepository) {
        this.reportRepository = reportRepository;
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
        this.rescueTaskRepository = rescueTaskRepository;
    }

    @Transactional
    public ReportDTO createReport(CreateReportRequest request, User submittedBy) {
        Report report = new Report();
        report.setSubmittedBy(submittedBy);
        report.setReportKind(ReportKind.EMERGENCY_REQUEST);
        report.setDetails(request.getDetails());
        report.setLocation(request.getLocation());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());

        Long createdDisasterId = null;
        if (request.getDisasterId() == null) {
            // create a minimal pending disaster so admins can see and verify
            Disaster disaster = new Disaster();
            String title = (request.getDetails() != null && !request.getDetails().isBlank())
                    ? (request.getDetails().length() > 80 ? request.getDetails().substring(0, 77) + "..." : request.getDetails())
                    : "Emergency Report";
            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                title = title + " - " + request.getLocation();
            }
            disaster.setTitle(title);
            disaster.setDescription(request.getDetails());
            disaster.setType(DisasterType.OTHER);
            disaster.setSeverity(DisasterSeverity.HIGH);
            disaster.setStatus(DisasterStatus.PENDING);
            disaster.setLocation(request.getLocation() != null ? request.getLocation() : (submittedBy.getRegion() != null ? submittedBy.getRegion() : "Unknown"));
            disaster.setRegion(submittedBy.getRegion());
            disaster.setCountry("India");
            disaster.setLatitude(request.getLatitude());
            disaster.setLongitude(request.getLongitude());
            disaster.setSource("CITIZEN_REPORT");
            disaster.setEventTime(LocalDateTime.now());

            Disaster savedDisaster = disasterRepository.save(disaster);
            report.setDisaster(savedDisaster);
            createdDisasterId = savedDisaster.getId();
        } else {
            disasterRepository.findById(request.getDisasterId()).ifPresent(report::setDisaster);
            createdDisasterId = request.getDisasterId();
        }

        User nearestResponder = findNearestAvailableResponder(
                submittedBy.getRegion(),
                createdDisasterId
        );
        if (nearestResponder != null) {
            report.setResponder(nearestResponder);
        }

        Report saved = reportRepository.save(report);
        return toDTO(saved, false);
    }

    @Transactional
    public Optional<ReportDTO> createIncidentReport(CreateIncidentReportRequest request, User responder) {
        if (responder.getRole() != UserRole.RESPONDER) {
            return Optional.empty();
        }
        if (request.getImageData() != null) {
            validateImagePayload(request.getImageData());
        }

        Report report = new Report();
        report.setSubmittedBy(responder);
        report.setReportKind(ReportKind.INCIDENT_REPORT);
        report.setDetails(request.getDetails());
        report.setLocation(request.getLocation());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        if (request.getImageData() != null && !request.getImageData().isBlank()) {
            report.setImageData(request.getImageData().trim());
        }

        if (request.getDisasterId() != null) {
            disasterRepository.findById(request.getDisasterId()).ifPresent(report::setDisaster);
        }

        if (request.getRescueTaskId() != null) {
            Optional<RescueTask> taskOpt = rescueTaskRepository.findById(request.getRescueTaskId())
                    .filter(t -> t.getResponder().getId().equals(responder.getId()));
            taskOpt.ifPresent(report::setRescueTask);
        }

        Report saved = reportRepository.save(report);
        return Optional.of(toDTO(saved, false));
    }

    private void validateImagePayload(String data) {
        if (data.length() > MAX_IMAGE_DATA_CHARS) {
            throw new IllegalArgumentException("Image too large; use a smaller image or lower resolution");
        }
        if (!data.startsWith("data:image/")) {
            throw new IllegalArgumentException("Image must be a data URL (data:image/...;base64,...)");
        }
    }

    private User findNearestAvailableResponder(String citizenRegion, Long disasterId) {
        String targetRegion = citizenRegion;
        if (targetRegion == null || targetRegion.isBlank()) {
            if (disasterId != null) {
                Disaster d = disasterRepository.findById(disasterId).orElse(null);
                if (d != null && d.getRegion() != null) targetRegion = d.getRegion();
            }
        }
        if (targetRegion == null || targetRegion.isBlank()) {
            targetRegion = "India";
        }

        List<User> respondersInRegion = userRepository.findByRegionAndRole(targetRegion.trim(), UserRole.RESPONDER);
        if (respondersInRegion.isEmpty()) {
            respondersInRegion = userRepository.findByRole(UserRole.RESPONDER);
        }

        return respondersInRegion.stream()
                .filter(r -> {
                    long activeCount = rescueTaskRepository.findActiveTasksByResponder(r.getId()).size();
                    return activeCount < 5;
                })
                .min(Comparator.comparingLong(r -> rescueTaskRepository.findActiveTasksByResponder(r.getId()).size()))
                .orElse(respondersInRegion.isEmpty() ? null : respondersInRegion.get(0));
    }

    public List<ReportDTO> getMyReports(Long userId) {
        return reportRepository.findBySubmittedByIdOrderBySubmittedAtDesc(userId).stream()
                .map(r -> toDTO(r, false))
                .collect(Collectors.toList());
    }

    public List<ReportDTO> getReportsForResponder(Long responderId) {
        return reportRepository.findByResponderIdOrderBySubmittedAtDesc(responderId).stream()
                .map(r -> toDTO(r, false))
                .collect(Collectors.toList());
    }

    public List<ReportDTO> getAllReportsForAudit() {
        return reportRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(r -> toDTO(r, false))
                .collect(Collectors.toList());
    }

    public Optional<ReportDTO> getReportById(Long id, User viewer) {
        return reportRepository.findById(id)
                .filter(report -> canViewReport(report, viewer))
                .map(report -> toDTO(report, true));
    }

    private boolean canViewReport(Report report, User viewer) {
        if (viewer.getRole() == UserRole.ADMIN) {
            return true;
        }
        if (report.getSubmittedBy().getId().equals(viewer.getId())) {
            return true;
        }
        if (viewer.getRole() == UserRole.RESPONDER
                && report.getResponder() != null
                && report.getResponder().getId().equals(viewer.getId())) {
            return true;
        }
        return false;
    }

    private ReportDTO toDTO(Report report, boolean includeImageData) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setReportKind(report.getReportKind().name());
        if (report.getDisaster() != null) {
            dto.setDisasterId(report.getDisaster().getId());
            dto.setDisasterTitle(report.getDisaster().getTitle());
        }
        if (report.getRescueTask() != null) {
            dto.setRescueTaskId(report.getRescueTask().getId());
        }
        if (report.getResponder() != null) {
            dto.setResponderId(report.getResponder().getId());
            dto.setResponderName(report.getResponder().getName());
        }
        dto.setSubmittedById(report.getSubmittedBy().getId());
        dto.setSubmittedByName(report.getSubmittedBy().getName());
        dto.setDetails(report.getDetails());
        dto.setLocation(report.getLocation());
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        boolean hasImage = report.getImageData() != null && !report.getImageData().isBlank();
        dto.setHasImage(hasImage);
        if (includeImageData && hasImage) {
            dto.setImageData(report.getImageData());
        }
        dto.setSubmittedAt(report.getSubmittedAt());
        return dto;
    }
}
