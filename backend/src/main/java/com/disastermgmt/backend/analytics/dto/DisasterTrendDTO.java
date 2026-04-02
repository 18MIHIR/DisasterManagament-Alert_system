package com.disastermgmt.backend.analytics.dto;

public class DisasterTrendDTO {
    private String monthYear; // e.g., "2024-01"
    private Long count;

    public DisasterTrendDTO(String monthYear, Long count) {
        this.monthYear = monthYear;
        this.count = count;
    }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
