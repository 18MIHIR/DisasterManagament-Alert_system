package com.disastermgmt.backend.rescuetask;

import jakarta.persistence.*;

@Entity
@Table(name = "rescue_zones")
public class RescueZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private Double centerLatitude;

    @Column(nullable = false)
    private Double centerLongitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Double getCenterLatitude() { return centerLatitude; }
    public void setCenterLatitude(Double centerLatitude) { this.centerLatitude = centerLatitude; }
    public Double getCenterLongitude() { return centerLongitude; }
    public void setCenterLongitude(Double centerLongitude) { this.centerLongitude = centerLongitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
