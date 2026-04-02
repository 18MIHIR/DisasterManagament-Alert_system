package com.disastermgmt.backend.rescuetask;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_tasks")
public class RescueTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id", nullable = false)
    private User responder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id", nullable = false)
    private Disaster disaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private RescueZone zone;

    private Double rescueSiteLatitude;

    private Double rescueSiteLongitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    private TaskStatus taskStatus = TaskStatus.ASSIGNED;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime acknowledgedAt;

    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public RescueTask() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getResponder() { return responder; }
    public void setResponder(User responder) { this.responder = responder; }
    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }
    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public RescueZone getZone() { return zone; }
    public void setZone(RescueZone zone) { this.zone = zone; }
    public Double getRescueSiteLatitude() { return rescueSiteLatitude; }
    public void setRescueSiteLatitude(Double rescueSiteLatitude) { this.rescueSiteLatitude = rescueSiteLatitude; }
    public Double getRescueSiteLongitude() { return rescueSiteLongitude; }
    public void setRescueSiteLongitude(Double rescueSiteLongitude) { this.rescueSiteLongitude = rescueSiteLongitude; }
}
