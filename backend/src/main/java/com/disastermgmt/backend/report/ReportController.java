package com.disastermgmt.backend.report;

import com.disastermgmt.backend.user.User;
import com.disastermgmt.backend.user.UserRepository;
import com.disastermgmt.backend.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createReport(@Valid @RequestBody CreateReportRequest request) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.CITIZEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Citizens can submit emergency help requests"));
        }
        try {
            ReportDTO created = reportService.createReport(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/incident")
    public ResponseEntity<?> createIncidentReport(@Valid @RequestBody CreateIncidentReportRequest request) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.RESPONDER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only Responders can submit incident reports"));
        }
        try {
            return reportService.createIncidentReport(request, user)
                    .<ResponseEntity<?>>map(d -> ResponseEntity.status(HttpStatus.CREATED).body(d))
                    .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid incident report")));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<ReportDTO>> getMyReports() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reportService.getMyReports(user.getId()));
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<List<ReportDTO>> getAssignedToMe() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.RESPONDER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reportService.getReportsForResponder(user.getId()));
    }

    @GetMapping("/audit")
    public ResponseEntity<?> auditTrail() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reportService.getAllReportsForAudit());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return reportService.getReportById(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
