package com.disastermgmt.backend.rescuetask;

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
@RequestMapping("/api/rescue-tasks")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RescueTaskController {

    private final RescueTaskService rescueTaskService;
    private final UserRepository userRepository;

    public RescueTaskController(RescueTaskService rescueTaskService, UserRepository userRepository) {
        this.rescueTaskService = rescueTaskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> listAllActive() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can list all rescue tasks"));
        }
        return ResponseEntity.ok(rescueTaskService.getActiveTasksForMap());
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<RescueTaskDTO>> getMyTasks() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.RESPONDER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(List.of());
        }
        return ResponseEntity.ok(rescueTaskService.getMyTasks(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody CreateRescueTaskRequest request) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admin can assign rescue tasks"));
        }
        var result = rescueTaskService.createTask(request, user.getId());
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid responder or disaster"));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<?> acknowledgeTask(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.RESPONDER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Responders can acknowledge tasks"));
        }
        var result = rescueTaskService.acknowledgeTask(id, user.getId());
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Task not found or already acknowledged"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.RESPONDER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Responders can update task status"));
        }
        return rescueTaskService.updateTaskStatus(id, user.getId(), status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/efficiency")
    public ResponseEntity<Map<String, Object>> getEfficiency() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(rescueTaskService.getAcknowledgmentEfficiency());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
