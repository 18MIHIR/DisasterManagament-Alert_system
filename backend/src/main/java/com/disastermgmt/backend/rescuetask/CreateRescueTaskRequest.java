package com.disastermgmt.backend.rescuetask;

import jakarta.validation.constraints.NotNull;

public class CreateRescueTaskRequest {

    @NotNull(message = "Responder ID is required")
    private Long responderId;

    @NotNull(message = "Disaster ID is required")
    private Long disasterId;

    private Long zoneId;

    private Double rescueSiteLatitude;

    private Double rescueSiteLongitude;

    private String description;

    public Long getResponderId() { return responderId; }
    public void setResponderId(Long responderId) { this.responderId = responderId; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public Double getRescueSiteLatitude() { return rescueSiteLatitude; }
    public void setRescueSiteLatitude(Double rescueSiteLatitude) { this.rescueSiteLatitude = rescueSiteLatitude; }
    public Double getRescueSiteLongitude() { return rescueSiteLongitude; }
    public void setRescueSiteLongitude(Double rescueSiteLongitude) { this.rescueSiteLongitude = rescueSiteLongitude; }
}
