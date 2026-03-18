package com.disastermgmt.backend.disaster;

import java.time.LocalDateTime;

public class UpdateDisasterRequest {
    private String title;
    private String description;
    private DisasterType type;
    private DisasterSeverity severity;
    private DisasterStatus status;
    private String location;
    private String region;
    private String country;
    private Double latitude;
    private Double longitude;
    private LocalDateTime eventTime;
    private String advisoryMessage;
    private String affectedAreas;
    private Integer estimatedAffectedPopulation;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DisasterType getType() { return type; }
    public void setType(DisasterType type) { this.type = type; }
    public DisasterSeverity getSeverity() { return severity; }
    public void setSeverity(DisasterSeverity severity) { this.severity = severity; }
    public DisasterStatus getStatus() { return status; }
    public void setStatus(DisasterStatus status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public String getAdvisoryMessage() { return advisoryMessage; }
    public void setAdvisoryMessage(String advisoryMessage) { this.advisoryMessage = advisoryMessage; }
    public String getAffectedAreas() { return affectedAreas; }
    public void setAffectedAreas(String affectedAreas) { this.affectedAreas = affectedAreas; }
    public Integer getEstimatedAffectedPopulation() { return estimatedAffectedPopulation; }
    public void setEstimatedAffectedPopulation(Integer estimatedAffectedPopulation) { this.estimatedAffectedPopulation = estimatedAffectedPopulation; }
}
