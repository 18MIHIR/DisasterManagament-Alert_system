package com.disastermgmt.backend.analytics.dto;

public class RegionPerformanceDTO {
    private String region;
    private Double avgResponseTimeDays; // Alternatively could be hours
    private Double resolutionEfficiency; // percentage of resolved/total
    private Long totalDisasters;

    public RegionPerformanceDTO(String region, Double avgResponseTimeDays, Double resolutionEfficiency, Long totalDisasters) {
        this.region = region;
        this.avgResponseTimeDays = avgResponseTimeDays;
        this.resolutionEfficiency = resolutionEfficiency;
        this.totalDisasters = totalDisasters;
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Double getAvgResponseTimeDays() { return avgResponseTimeDays; }
    public void setAvgResponseTimeDays(Double avgResponseTimeDays) { this.avgResponseTimeDays = avgResponseTimeDays; }
    public Double getResolutionEfficiency() { return resolutionEfficiency; }
    public void setResolutionEfficiency(Double resolutionEfficiency) { this.resolutionEfficiency = resolutionEfficiency; }
    public Long getTotalDisasters() { return totalDisasters; }
    public void setTotalDisasters(Long totalDisasters) { this.totalDisasters = totalDisasters; }
}
