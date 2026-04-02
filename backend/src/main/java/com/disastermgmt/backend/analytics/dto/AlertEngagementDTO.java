package com.disastermgmt.backend.analytics.dto;

public class AlertEngagementDTO {
    private Long totalBroadcasted;
    private Long totalAcknowledged;
    private Long totalIgnored;
    private Double engagementRate; // percentage

    public AlertEngagementDTO(Long totalBroadcasted, Long totalAcknowledged, Long totalIgnored, Double engagementRate) {
        this.totalBroadcasted = totalBroadcasted;
        this.totalAcknowledged = totalAcknowledged;
        this.totalIgnored = totalIgnored;
        this.engagementRate = engagementRate;
    }

    public Long getTotalBroadcasted() { return totalBroadcasted; }
    public void setTotalBroadcasted(Long totalBroadcasted) { this.totalBroadcasted = totalBroadcasted; }
    public Long getTotalAcknowledged() { return totalAcknowledged; }
    public void setTotalAcknowledged(Long totalAcknowledged) { this.totalAcknowledged = totalAcknowledged; }
    public Long getTotalIgnored() { return totalIgnored; }
    public void setTotalIgnored(Long totalIgnored) { this.totalIgnored = totalIgnored; }
    public Double getEngagementRate() { return engagementRate; }
    public void setEngagementRate(Double engagementRate) { this.engagementRate = engagementRate; }
}
