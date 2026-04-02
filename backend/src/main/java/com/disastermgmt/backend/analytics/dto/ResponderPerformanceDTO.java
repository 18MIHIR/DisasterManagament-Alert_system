package com.disastermgmt.backend.analytics.dto;

public class ResponderPerformanceDTO {
    private String responderName;
    private Long totalAssignedTasks;
    private Long completedTasks;
    private Double completionRate; // percentage

    public ResponderPerformanceDTO(String responderName, Long totalAssignedTasks, Long completedTasks, Double completionRate) {
        this.responderName = responderName;
        this.totalAssignedTasks = totalAssignedTasks;
        this.completedTasks = completedTasks;
        this.completionRate = completionRate;
    }

    public String getResponderName() { return responderName; }
    public void setResponderName(String responderName) { this.responderName = responderName; }
    public Long getTotalAssignedTasks() { return totalAssignedTasks; }
    public void setTotalAssignedTasks(Long totalAssignedTasks) { this.totalAssignedTasks = totalAssignedTasks; }
    public Long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Long completedTasks) { this.completedTasks = completedTasks; }
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}
