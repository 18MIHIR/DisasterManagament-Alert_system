package com.disastermgmt.backend.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getRegion()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setRegion(request.getRegion());

        userRepository.save(user);

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getRegion()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/responders")
    public ResponseEntity<List<UserProfileResponse>> getResponders(Authentication authentication) {
        List<User> responders = userRepository.findByRole(UserRole.RESPONDER);
        List<UserProfileResponse> list = responders.stream()
                .map(u -> new UserProfileResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getPhone(), u.getRegion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}

