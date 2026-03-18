package com.disastermgmt.backend.rescuetask;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.disaster.DisasterRepository;
import com.disastermgmt.backend.user.User;
import com.disastermgmt.backend.user.UserRepository;
import com.disastermgmt.backend.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RescueTaskService {

    private final RescueTaskRepository rescueTaskRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;

    public RescueTaskService(RescueTaskRepository rescueTaskRepository,
                             DisasterRepository disasterRepository,
                             UserRepository userRepository) {
        this.rescueTaskRepository = rescueTaskRepository;
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
    }

    public List<RescueTaskDTO> getMyTasks(Long responderId) {
        return rescueTaskRepository.findByResponderIdOrderByUpdatedAtDesc(responderId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<RescueTaskDTO> createTask(CreateRescueTaskRequest request, Long adminId) {
        User responder = userRepository.findById(request.getResponderId()).orElse(null);
        Disaster disaster = disasterRepository.findById(request.getDisasterId()).orElse(null);
        if (responder == null || disaster == null || responder.getRole() != UserRole.RESPONDER) {
            return Optional.empty();
        }

        RescueTask task = new RescueTask();
        task.setResponder(responder);
        task.setDisaster(disaster);
        task.setTaskStatus(TaskStatus.ASSIGNED);
        task.setDescription(request.getDescription() != null ? request.getDescription() : "Respond to disaster: " + disaster.getTitle());
        RescueTask saved = rescueTaskRepository.save(task);
        return Optional.of(toDTO(saved));
    }

    @Transactional
    public Optional<RescueTaskDTO> acknowledgeTask(Long taskId, Long responderId) {
        return rescueTaskRepository.findById(taskId)
                .filter(t -> t.getResponder().getId().equals(responderId))
                .filter(t -> t.getTaskStatus() == TaskStatus.ASSIGNED)
                .map(task -> {
                    task.setTaskStatus(TaskStatus.ACKNOWLEDGED);
                    task.setAcknowledgedAt(LocalDateTime.now());
                    return rescueTaskRepository.save(task);
                })
                .map(this::toDTO);
    }

    @Transactional
    public Optional<RescueTaskDTO> updateTaskStatus(Long taskId, Long responderId, TaskStatus newStatus) {
        return rescueTaskRepository.findById(taskId)
                .filter(t -> t.getResponder().getId().equals(responderId))
                .map(task -> {
                    task.setTaskStatus(newStatus);
                    return rescueTaskRepository.save(task);
                })
                .map(this::toDTO);
    }

    public Map<String, Object> getAcknowledgmentEfficiency() {
        List<RescueTask> acknowledged = rescueTaskRepository.findAll().stream()
                .filter(t -> t.getAcknowledgedAt() != null)
                .collect(Collectors.toList());

        if (acknowledged.isEmpty()) {
            return Map.of(
                "totalAcknowledged", 0,
                "averageResponseMinutes", 0.0,
                "message", "No acknowledgment data yet"
            );
        }

        double avgMinutes = acknowledged.stream()
                .mapToLong(t -> {
                    LocalDateTime assigned = t.getAssignedAt() != null ? t.getAssignedAt() : t.getUpdatedAt();
                    LocalDateTime ack = t.getAcknowledgedAt();
                    return Duration.between(assigned, ack).toMinutes();
                })
                .average()
                .orElse(0);

        return Map.of(
            "totalAcknowledged", acknowledged.size(),
            "averageResponseMinutes", Math.round(avgMinutes * 10) / 10.0,
            "message", "Acknowledgment times logged successfully"
        );
    }

    private RescueTaskDTO toDTO(RescueTask task) {
        RescueTaskDTO dto = new RescueTaskDTO();
        dto.setId(task.getId());
        dto.setResponderId(task.getResponder().getId());
        dto.setResponderName(task.getResponder().getName());
        dto.setDisasterId(task.getDisaster().getId());
        dto.setDisasterTitle(task.getDisaster().getTitle());
        dto.setDisasterLocation(task.getDisaster().getLocation());
        dto.setTaskStatus(task.getTaskStatus());
        dto.setDescription(task.getDescription());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setAcknowledgedAt(task.getAcknowledgedAt());
        return dto;
    }
}
