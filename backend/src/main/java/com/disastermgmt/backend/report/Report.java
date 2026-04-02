package com.disastermgmt.backend.report;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.rescuetask.RescueTask;
import com.disastermgmt.backend.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id")
    private Disaster disaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id")
    private User responder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_kind", nullable = false)
    private ReportKind reportKind = ReportKind.EMERGENCY_REQUEST;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_task_id")
    private RescueTask rescueTask;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String details;

    private String location;

    private Double latitude;

    private Double longitude;

    @Column(columnDefinition = "LONGTEXT")
    private String imageData;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    public Report() {}

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }
    public User getResponder() { return responder; }
    public void setResponder(User responder) { this.responder = responder; }
    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public ReportKind getReportKind() { return reportKind; }
    public void setReportKind(ReportKind reportKind) { this.reportKind = reportKind; }
    public RescueTask getRescueTask() { return rescueTask; }
    public void setRescueTask(RescueTask rescueTask) { this.rescueTask = rescueTask; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }
}
