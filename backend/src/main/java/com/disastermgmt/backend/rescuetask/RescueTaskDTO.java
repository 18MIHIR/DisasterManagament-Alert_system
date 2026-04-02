package com.disastermgmt.backend.rescuetask;

import java.time.LocalDateTime;

public class RescueTaskDTO {
    private Long id;
    private Long responderId;
    private String responderName;
    private Long disasterId;
    private String disasterTitle;
    private String disasterLocation;
    private Double disasterLatitude;
    private Double disasterLongitude;
    private Long zoneId;
    private String zoneName;
    private Double rescueSiteLatitude;
    private Double rescueSiteLongitude;
    private TaskStatus taskStatus;
    private String description;
    private LocalDateTime updatedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime assignedAt;

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
    public Double getDisasterLatitude() { return disasterLatitude; }
    public void setDisasterLatitude(Double disasterLatitude) { this.disasterLatitude = disasterLatitude; }
    public Double getDisasterLongitude() { return disasterLongitude; }
    public void setDisasterLongitude(Double disasterLongitude) { this.disasterLongitude = disasterLongitude; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public Double getRescueSiteLatitude() { return rescueSiteLatitude; }
    public void setRescueSiteLatitude(Double rescueSiteLatitude) { this.rescueSiteLatitude = rescueSiteLatitude; }
    public Double getRescueSiteLongitude() { return rescueSiteLongitude; }
    public void setRescueSiteLongitude(Double rescueSiteLongitude) { this.rescueSiteLongitude = rescueSiteLongitude; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
