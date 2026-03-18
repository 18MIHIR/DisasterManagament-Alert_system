package com.disastermgmt.backend.report;

import java.time.LocalDateTime;

public class ReportDTO {
    private Long id;
    private Long disasterId;
    private String disasterTitle;
    private Long responderId;
    private String responderName;
    private Long submittedById;
    private String submittedByName;
    private String details;
    private String location;
    private LocalDateTime submittedAt;

    public ReportDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDisasterId() { return disasterId; }
    public void setDisasterId(Long disasterId) { this.disasterId = disasterId; }
    public String getDisasterTitle() { return disasterTitle; }
    public void setDisasterTitle(String disasterTitle) { this.disasterTitle = disasterTitle; }
    public Long getResponderId() { return responderId; }
    public void setResponderId(Long responderId) { this.responderId = responderId; }
    public String getResponderName() { return responderName; }
    public void setResponderName(String responderName) { this.responderName = responderName; }
    public Long getSubmittedById() { return submittedById; }
    public void setSubmittedById(Long submittedById) { this.submittedById = submittedById; }
    public String getSubmittedByName() { return submittedByName; }
    public void setSubmittedByName(String submittedByName) { this.submittedByName = submittedByName; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
