package com.disastermgmt.backend.rescuetask;

import java.time.LocalDateTime;

public class RescueTaskDTO {
    private Long id;
    private Long responderId;
    private String responderName;
    private Long disasterId;
    private String disasterTitle;
    private String disasterLocation;
    private TaskStatus taskStatus;
    private String description;
    private LocalDateTime updatedAt;
    private LocalDateTime acknowledgedAt;

    public RescueTaskDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResponderId() { return responderId; }
    public void setResponderId(Long responderId) { this.responderId = responderId; }
    public String getResponderName() { return responderName; }
    public void setResponderName(String responderName) { this.responderName = responderName; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getDisasterTitle() { return disasterTitle; }
    public void setDisasterTitle(String disasterTitle) { this.disasterTitle = disasterTitle; }
    public String getDisasterLocation() { return disasterLocation; }
    public void setDisasterLocation(String disasterLocation) { this.disasterLocation = disasterLocation; }
    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
