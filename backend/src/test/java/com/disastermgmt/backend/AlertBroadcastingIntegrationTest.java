package com.disastermgmt.backend;

import com.disastermgmt.backend.alert.AlertRepository;
import com.disastermgmt.backend.auth.AuthService;
import com.disastermgmt.backend.auth.LoginRequest;
import com.disastermgmt.backend.auth.RegisterRequest;
import com.disastermgmt.backend.auth.AuthResponse;
import com.disastermgmt.backend.disaster.DisasterRepository;
import com.disastermgmt.backend.disaster.DisasterService;
import com.disastermgmt.backend.disaster.DisasterStatus;
import com.disastermgmt.backend.disaster.DisasterType;
import com.disastermgmt.backend.disaster.DisasterSeverity;
import com.disastermgmt.backend.disaster.CreateDisasterRequest;
import com.disastermgmt.backend.user.UserRepository;
import com.disastermgmt.backend.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AlertBroadcastingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private DisasterService disasterService;

    @Autowired
    private DisasterRepository disasterRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String citizenToken;
    private Long disasterId;
    private String adminEmail;
    private String citizenEmail;

    @BeforeEach
    void setUp() throws Exception {
        alertRepository.deleteAll();
        disasterRepository.deleteAll();
        userRepository.deleteAll();
        String suffix = String.valueOf(System.nanoTime());
        adminEmail = "admin-" + suffix + "@test.com";
        citizenEmail = "citizen-" + suffix + "@test.com";

        RegisterRequest adminReq = new RegisterRequest();
        adminReq.setName("Admin User"); adminReq.setEmail(adminEmail); adminReq.setPassword("pass123");
        adminReq.setRole(UserRole.ADMIN); adminReq.setPhone("1234567890"); adminReq.setRegion("Maharashtra");
        AuthResponse admin = authService.register(adminReq);
        adminToken = admin.getToken();

        RegisterRequest citizenReq = new RegisterRequest();
        citizenReq.setName("Citizen User"); citizenReq.setEmail(citizenEmail); citizenReq.setPassword("pass123");
        citizenReq.setRole(UserRole.CITIZEN); citizenReq.setPhone("9876543210"); citizenReq.setRegion("Maharashtra");
        AuthResponse citizen = authService.register(citizenReq);
        citizenToken = citizen.getToken();

        CreateDisasterRequest req = new CreateDisasterRequest();
        req.setTitle("Flood Alert");
        req.setType(DisasterType.FLOOD);
        req.setSeverity(DisasterSeverity.HIGH);
        req.setLocation("Mumbai");
        req.setRegion("Maharashtra");
        req.setEventTime(LocalDateTime.now());

        disasterService.createDisaster(req, null);
        disasterId = disasterRepository.findAll().get(0).getId();
    }

    @Test
    void adminCanVerifyAndPushAlert() throws Exception {
        mockMvc.perform(post("/api/disasters/{id}/verify", disasterId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.alertBroadcastAt").isNotEmpty());

        assertThat(alertRepository.count()).isEqualTo(1);
        assertThat(alertRepository.findAll().get(0).getRegion()).isEqualTo("Maharashtra");
    }

    @Test
    void citizenInAffectedRegionReceivesAlerts() throws Exception {
        Long adminId = userRepository.findByEmail(adminEmail).orElseThrow().getId();
        disasterService.verifyAndPushAlert(disasterId, adminId);

        mockMvc.perform(get("/api/alerts/for-citizen")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].region").value("Maharashtra"))
                .andExpect(jsonPath("$[0].message").isNotEmpty());
    }

    @Test
    void citizenInOtherRegionReceivesNoAlerts() throws Exception {
        RegisterRequest otherReq = new RegisterRequest();
        otherReq.setName("Other Citizen"); otherReq.setEmail("other-" + System.nanoTime() + "@test.com"); otherReq.setPassword("pass123");
        otherReq.setRole(UserRole.CITIZEN); otherReq.setPhone("1111111111"); otherReq.setRegion("Karnataka");
        AuthResponse otherCitizen = authService.register(otherReq);

        Long adminId = userRepository.findByEmail(adminEmail).orElseThrow().getId();
        disasterService.verifyAndPushAlert(disasterId, adminId);

        mockMvc.perform(get("/api/alerts/for-citizen")
                        .header("Authorization", "Bearer " + otherCitizen.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
