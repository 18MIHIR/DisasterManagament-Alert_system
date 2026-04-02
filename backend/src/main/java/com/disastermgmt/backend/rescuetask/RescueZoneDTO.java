package com.disastermgmt.backend.rescuetask;

public class RescueZoneDTO {
    private Long id;
    private String name;
    private String region;
    private Double centerLatitude;
    private Double centerLongitude;
    private String description;

    public static RescueZoneDTO from(RescueZone z) {
        RescueZoneDTO d = new RescueZoneDTO();
        d.setId(z.getId());
        d.setName(z.getName());
        d.setRegion(z.getRegion());
        d.setCenterLatitude(z.getCenterLatitude());
        d.setCenterLongitude(z.getCenterLongitude());
        d.setDescription(z.getDescription());
        return d;
    }

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
