package com.disastermgmt.backend.alert;

import java.time.LocalDateTime;

public class AlertDTO {
    private Long id;
    private Long disasterId;
    private String disasterTitle;
    private String message;
    private Long createdById;
    private String createdByName;
    private LocalDateTime broadcastTime;
    private String region;

    public AlertDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getDisasterTitle() { return disasterTitle; }
    public void setDisasterTitle(String disasterTitle) { this.disasterTitle = disasterTitle; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public LocalDateTime getBroadcastTime() { return broadcastTime; }
    public void setBroadcastTime(LocalDateTime broadcastTime) { this.broadcastTime = broadcastTime; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
