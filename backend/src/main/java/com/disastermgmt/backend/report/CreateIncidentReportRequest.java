package com.disastermgmt.backend.report;

import jakarta.validation.constraints.NotBlank;

public class CreateIncidentReportRequest {

    private Long disasterId;

    private Long rescueTaskId;

    @NotBlank(message = "Details are required")
    private String details;

    private String location;

    private Double latitude;

    private Double longitude;

    private String imageData;

    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public Long getRescueTaskId() { return rescueTaskId; }
    public void setRescueTaskId(Long rescueTaskId) { this.rescueTaskId = rescueTaskId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }
}
