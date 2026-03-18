package com.disastermgmt.backend.rescuetask;

import jakarta.validation.constraints.NotNull;

public class CreateRescueTaskRequest {

    @NotNull(message = "Responder ID is required")
    private Long responderId;

    @NotNull(message = "Disaster ID is required")
    private Long disasterId;

    private String description;

    public Long getResponderId() { return responderId; }
    public void setResponderId(Long responderId) { this.responderId = responderId; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
