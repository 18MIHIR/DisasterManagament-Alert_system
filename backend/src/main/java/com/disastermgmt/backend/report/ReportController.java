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
                    .body(new Object() {
                        public final String error = "Only Citizens can submit emergency help requests";
                    });
        }
        ReportDTO created = reportService.createReport(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
