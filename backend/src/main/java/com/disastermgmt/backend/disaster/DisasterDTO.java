package com.disastermgmt.backend.disaster;

import java.time.LocalDateTime;

public class DisasterDTO {
    private Long id;
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
    private String source;
    private LocalDateTime eventTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private String advisoryMessage;
    private String affectedAreas;
    private Integer estimatedAffectedPopulation;
    private Long verifiedBy;
    private java.time.LocalDateTime alertBroadcastAt;

    public DisasterDTO() {}

    public static DisasterDTO fromEntity(Disaster disaster) {
        DisasterDTO dto = new DisasterDTO();
        dto.setId(disaster.getId());
        dto.setTitle(disaster.getTitle());
        dto.setDescription(disaster.getDescription());
        dto.setType(disaster.getType());
        dto.setSeverity(disaster.getSeverity());
        dto.setStatus(disaster.getStatus());
        dto.setLocation(disaster.getLocation());
        dto.setRegion(disaster.getRegion());
        dto.setCountry(disaster.getCountry());
        dto.setLatitude(disaster.getLatitude());
        dto.setLongitude(disaster.getLongitude());
        dto.setSource(disaster.getSource());
        dto.setEventTime(disaster.getEventTime());
        dto.setCreatedAt(disaster.getCreatedAt());
        dto.setUpdatedAt(disaster.getUpdatedAt());
        dto.setResolvedAt(disaster.getResolvedAt());
        dto.setAdvisoryMessage(disaster.getAdvisoryMessage());
        dto.setAffectedAreas(disaster.getAffectedAreas());
        dto.setEstimatedAffectedPopulation(disaster.getEstimatedAffectedPopulation());
        dto.setVerifiedBy(disaster.getVerifiedBy());
        dto.setAlertBroadcastAt(disaster.getAlertBroadcastAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getAdvisoryMessage() { return advisoryMessage; }
    public void setAdvisoryMessage(String advisoryMessage) { this.advisoryMessage = advisoryMessage; }
    public String getAffectedAreas() { return affectedAreas; }
    public void setAffectedAreas(String affectedAreas) { this.affectedAreas = affectedAreas; }
    public Integer getEstimatedAffectedPopulation() { return estimatedAffectedPopulation; }
    public void setEstimatedAffectedPopulation(Integer estimatedAffectedPopulation) { this.estimatedAffectedPopulation = estimatedAffectedPopulation; }
    public Long getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(Long verifiedBy) { this.verifiedBy = verifiedBy; }
    public java.time.LocalDateTime getAlertBroadcastAt() { return alertBroadcastAt; }
    public void setAlertBroadcastAt(java.time.LocalDateTime alertBroadcastAt) { this.alertBroadcastAt = alertBroadcastAt; }
}
