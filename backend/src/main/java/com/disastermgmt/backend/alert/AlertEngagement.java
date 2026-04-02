package com.disastermgmt.backend.alert;

import com.disastermgmt.backend.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_engagements")
public class AlertEngagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean acknowledged = false;

    @Column(nullable = false)
    private LocalDateTime engagedAt;

    @PrePersist
    protected void onCreate() {
        engagedAt = LocalDateTime.now();
    }

    public AlertEngagement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alert getAlert() { return alert; }
    public void setAlert(Alert alert) { this.alert = alert; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    public LocalDateTime getEngagedAt() { return engagedAt; }
    public void setEngagedAt(LocalDateTime engagedAt) { this.engagedAt = engagedAt; }
}
