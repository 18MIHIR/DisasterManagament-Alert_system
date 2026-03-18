package com.disastermgmt.backend.report;

import jakarta.validation.constraints.NotBlank;

public class CreateReportRequest {

    private Long disasterId;

    @NotBlank(message = "Details are required")
    private String details;

    private String location;

    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
