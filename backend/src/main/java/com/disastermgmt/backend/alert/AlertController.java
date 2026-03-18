package com.disastermgmt.backend.alert;

import com.disastermgmt.backend.user.User;
import com.disastermgmt.backend.user.UserRepository;
import com.disastermgmt.backend.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AlertController {

    private final AlertService alertService;
    private final UserRepository userRepository;

    public AlertController(AlertService alertService, UserRepository userRepository) {
        this.alertService = alertService;
        this.userRepository = userRepository;
    }

    @GetMapping("/for-citizen")
    public ResponseEntity<List<AlertDTO>> getAlertsForCitizen() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRegion() == null || user.getRegion().isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(alertService.getAlertsForCitizenRegion(user.getRegion()));
    }

    @GetMapping
    public ResponseEntity<List<AlertDTO>> getAllAlerts() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
