package com.disastermgmt.backend.disaster;

import com.disastermgmt.backend.external.DisasterDataScheduler;
import com.disastermgmt.backend.security.JwtService;
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
@RequestMapping("/api/disasters")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class DisasterController {

    private final DisasterService disasterService;
    private final DisasterDataScheduler disasterDataScheduler;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public DisasterController(DisasterService disasterService,
                              DisasterDataScheduler disasterDataScheduler,
                              JwtService jwtService,
                              UserRepository userRepository) {
        this.disasterService = disasterService;
        this.disasterDataScheduler = disasterDataScheduler;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<DisasterDTO>> getAllDisasters(
            @RequestParam(required = false) DisasterType type,
            @RequestParam(required = false) DisasterSeverity severity,
            @RequestParam(required = false) DisasterStatus status,
            @RequestParam(required = false) String region) {
        
        if (type == null && severity == null && status == null && region == null) {
            return ResponseEntity.ok(disasterService.getAllDisasters());
        }
        return ResponseEntity.ok(disasterService.getDisastersWithFilters(type, severity, status, region));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisasterDTO> getDisasterById(@PathVariable Long id) {
        return disasterService.getDisasterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DisasterDTO>> getActiveDisasters() {
        return ResponseEntity.ok(disasterService.getActiveDisasters());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<DisasterDTO>> getPendingDisasters() {
        return ResponseEntity.ok(disasterService.getPendingDisasters());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<DisasterDTO>> getRecentDisasters(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(disasterService.getRecentDisasters(hours));
    }

    @GetMapping("/stats")
    public ResponseEntity<DisasterService.DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(disasterService.getDashboardStats());
    }

    @GetMapping("/types")
    public ResponseEntity<DisasterType[]> getDisasterTypes() {
        return ResponseEntity.ok(DisasterType.values());
    }

    @GetMapping("/severities")
    public ResponseEntity<DisasterSeverity[]> getSeverityLevels() {
        return ResponseEntity.ok(DisasterSeverity.values());
    }

    @GetMapping("/statuses")
    public ResponseEntity<DisasterStatus[]> getStatusOptions() {
        return ResponseEntity.ok(DisasterStatus.values());
    }

    @PostMapping
    public ResponseEntity<?> createDisaster(
            @Valid @RequestBody CreateDisasterRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.RESPONDER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin and Responder can report disasters"));
        }
        Long reportedBy = (currentUser.getRole() == UserRole.RESPONDER) ? currentUser.getId() : null;
        DisasterDTO created = disasterService.createDisaster(request, reportedBy);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisasterDTO> updateDisaster(
            @PathVariable Long id,
            @RequestBody UpdateDisasterRequest request) {
        return disasterService.updateDisaster(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyAndPushAlert(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can verify and push alert notifications"));
        }
        return disasterService.verifyAndPushAlert(id, currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveDisaster(@PathVariable Long id) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can resolve disasters"));
        }
        return disasterService.resolveDisaster(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelDisaster(@PathVariable Long id) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can cancel disaster alerts"));
        }
        return disasterService.cancelDisaster(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDisaster(@PathVariable Long id) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can delete disasters"));
        }
        disasterService.deleteDisaster(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/pending")
    public ResponseEntity<?> deleteAllPendingDisasters() {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can remove pending alerts"));
        }
        int deleted = disasterService.deleteAllPendingDisasters();
        return ResponseEntity.ok(Map.of("deleted", deleted, "message", "Removed " + deleted + " pending alert(s)"));
    }

    @PostMapping("/fetch-external")
    public ResponseEntity<?> triggerExternalFetch() {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can trigger external data fetch"));
        }
        disasterDataScheduler.triggerManualFetch();
        return ResponseEntity.ok(Map.of("message", "External data fetch triggered successfully"));
    }

    private boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
