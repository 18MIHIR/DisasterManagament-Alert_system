package com.disastermgmt.backend;

import com.disastermgmt.backend.auth.AuthResponse;
import com.disastermgmt.backend.auth.RegisterRequest;
import com.disastermgmt.backend.auth.AuthService;
import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.disaster.DisasterRepository;
import com.disastermgmt.backend.disaster.DisasterType;
import com.disastermgmt.backend.disaster.DisasterSeverity;
import com.disastermgmt.backend.rescuetask.RescueTaskRepository;
import com.disastermgmt.backend.rescuetask.TaskStatus;
import com.disastermgmt.backend.user.UserRepository;
import com.disastermgmt.backend.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RescueTaskAcknowledgmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private RescueTaskRepository rescueTaskRepository;

    @Autowired
    private DisasterRepository disasterRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String responderToken;
    private Long disasterId;
    private Long responderId;
    private String responderEmail;

    @BeforeEach
    void setUp() throws Exception {
        rescueTaskRepository.deleteAll();
        disasterRepository.deleteAll();
        userRepository.deleteAll();
        String suffix = String.valueOf(System.nanoTime());
        responderEmail = "responder-" + suffix + "@test.com";

        RegisterRequest adminReq = new RegisterRequest();
        adminReq.setName("Admin User"); adminReq.setEmail("admin-" + suffix + "@test.com"); adminReq.setPassword("pass123");
        adminReq.setRole(UserRole.ADMIN); adminReq.setPhone("1234567890"); adminReq.setRegion("Maharashtra");
        AuthResponse admin = authService.register(adminReq);
        adminToken = admin.getToken();

        RegisterRequest responderReq = new RegisterRequest();
        responderReq.setName("Responder User"); responderReq.setEmail(responderEmail); responderReq.setPassword("pass123");
        responderReq.setRole(UserRole.RESPONDER); responderReq.setPhone("9876543210"); responderReq.setRegion("Maharashtra");
        AuthResponse responder = authService.register(responderReq);
        responderToken = responder.getToken();
        responderId = userRepository.findByEmail(responderEmail).orElseThrow().getId();

        Disaster d = new Disaster();
        d.setTitle("Test Disaster");
        d.setType(DisasterType.FLOOD);
        d.setSeverity(DisasterSeverity.HIGH);
        d.setStatus(com.disastermgmt.backend.disaster.DisasterStatus.ACTIVE);
        d.setLocation("Mumbai");
        d.setRegion("Maharashtra");
        d.setEventTime(LocalDateTime.now());
        d.setCreatedAt(LocalDateTime.now());
        disasterRepository.save(d);
        disasterId = d.getId();
    }

    @Test
    void responderCanAcknowledgeTask() throws Exception {
        String createBody = String.format(
                "{\"responderId\":%d,\"disasterId\":%d,\"description\":\"Rescue operation\"}",
                responderId, disasterId);

        mockMvc.perform(post("/api/rescue-tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("ASSIGNED"));

        Long taskId = rescueTaskRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/rescue-tasks/{id}/acknowledge", taskId)
                        .header("Authorization", "Bearer " + responderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.acknowledgedAt").isNotEmpty());

        assertThat(rescueTaskRepository.findById(taskId).orElseThrow().getAcknowledgedAt()).isNotNull();
    }

    @Test
    void acknowledgmentTimeIsLogged() throws Exception {
        String createBody = String.format(
                "{\"responderId\":%d,\"disasterId\":%d,\"description\":\"Test\"}",
                responderId, disasterId);

        mockMvc.perform(post("/api/rescue-tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk());

        Long taskId = rescueTaskRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/rescue-tasks/{id}/acknowledge", taskId)
                        .header("Authorization", "Bearer " + responderToken))
                .andExpect(status().isOk());

        var task = rescueTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getAcknowledgedAt()).isNotNull();
        assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.ACKNOWLEDGED);
    }

    @Test
    void adminCanViewAcknowledgmentEfficiency() throws Exception {
        mockMvc.perform(get("/api/rescue-tasks/efficiency")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAcknowledged").exists())
                .andExpect(jsonPath("$.averageResponseMinutes").exists());
    }
}
