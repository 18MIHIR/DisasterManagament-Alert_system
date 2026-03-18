package com.disastermgmt.backend.report;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.disaster.DisasterRepository;
import com.disastermgmt.backend.rescuetask.RescueTaskRepository;
import com.disastermgmt.backend.rescuetask.TaskStatus;
import com.disastermgmt.backend.user.User;
import com.disastermgmt.backend.user.UserRepository;
import com.disastermgmt.backend.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportService {

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
        report.setDetails(request.getDetails());
        report.setLocation(request.getLocation());

        if (request.getDisasterId() != null) {
            disasterRepository.findById(request.getDisasterId()).ifPresent(report::setDisaster);
        }

        User nearestResponder = findNearestAvailableResponder(
                submittedBy.getRegion(),
                request.getDisasterId()
        );
        if (nearestResponder != null) {
            report.setResponder(nearestResponder);
        }

        Report saved = reportRepository.save(report);
        return toDTO(saved);
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
                .min(Comparator.comparingLong(r ->
                        rescueTaskRepository.findActiveTasksByResponder(r.getId()).size()
                ))
                .orElse(respondersInRegion.isEmpty() ? null : respondersInRegion.get(0));
    }

    public List<ReportDTO> getMyReports(Long userId) {
        return reportRepository.findBySubmittedByIdOrderBySubmittedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ReportDTO> getReportsForResponder(Long responderId) {
        return reportRepository.findByResponderIdOrderBySubmittedAtDesc(responderId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ReportDTO toDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        if (report.getDisaster() != null) {
            dto.setDisasterId(report.getDisaster().getId());
            dto.setDisasterTitle(report.getDisaster().getTitle());
        }
        if (report.getResponder() != null) {
            dto.setResponderId(report.getResponder().getId());
            dto.setResponderName(report.getResponder().getName());
        }
        dto.setSubmittedById(report.getSubmittedBy().getId());
        dto.setSubmittedByName(report.getSubmittedBy().getName());
        dto.setDetails(report.getDetails());
        dto.setLocation(report.getLocation());
        dto.setSubmittedAt(report.getSubmittedAt());
        return dto;
    }
}
