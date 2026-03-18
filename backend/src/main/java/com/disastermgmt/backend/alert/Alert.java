package com.disastermgmt.backend.alert;

import com.disastermgmt.backend.disaster.Disaster;
import com.disastermgmt.backend.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disaster_id", nullable = false)
    private Disaster disaster;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime broadcastTime;

    @Column(nullable = false)
    private String region;

    public Alert() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Disaster getDisaster() { return disaster; }
    public void setDisaster(Disaster disaster) { this.disaster = disaster; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getBroadcastTime() { return broadcastTime; }
    public void setBroadcastTime(LocalDateTime broadcastTime) { this.broadcastTime = broadcastTime; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
