package com.disastermgmt.backend.analytics;

import com.disastermgmt.backend.alert.AlertEngagementRepository;
import com.disastermgmt.backend.alert.AlertRepository;
import com.disastermgmt.backend.analytics.dto.AlertEngagementDTO;
import com.disastermgmt.backend.analytics.dto.DisasterTrendDTO;
import com.disastermgmt.backend.analytics.dto.RegionPerformanceDTO;
import com.disastermgmt.backend.analytics.dto.ResponderPerformanceDTO;
import com.disastermgmt.backend.disaster.DisasterRepository;
import com.disastermgmt.backend.disaster.DisasterStatus;
import com.disastermgmt.backend.rescuetask.RescueTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final DisasterRepository disasterRepository;
    private final RescueTaskRepository rescueTaskRepository;
    private final AlertRepository alertRepository;
    private final AlertEngagementRepository alertEngagementRepository;

    public AnalyticsService(DisasterRepository disasterRepository,
                            RescueTaskRepository rescueTaskRepository,
                            AlertRepository alertRepository,
                            AlertEngagementRepository alertEngagementRepository) {
        this.disasterRepository = disasterRepository;
        this.rescueTaskRepository = rescueTaskRepository;
        this.alertRepository = alertRepository;
        this.alertEngagementRepository = alertEngagementRepository;
    }

    public List<DisasterTrendDTO> getDisasterTrends() {
        List<LocalDateTime> dates = disasterRepository.findAllCreationDates();
        
        // Mock data if empty for dashboard visualization
        if (dates.isEmpty()) {
            return generateMockTrends();
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> grouped = dates.stream()
                .collect(Collectors.groupingBy(d -> d.format(fmt), Collectors.counting()));

        return grouped.entrySet().stream()
                .map(e -> new DisasterTrendDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DisasterTrendDTO::getMonthYear))
                .collect(Collectors.toList());
    }

    public List<RegionPerformanceDTO> getRegionPerformance() {
        List<Object[]> rawStats = disasterRepository.findAllRegionStats();
        
        if (rawStats.isEmpty()) {
            return generateMockRegionPerformance();
        }

        Map<String, RegionStats> statsMap = new HashMap<>();

        for (Object[] row : rawStats) {
            String region = (String) row[0];
            LocalDateTime createdAt = (LocalDateTime) row[1];
            LocalDateTime resolvedAt = (LocalDateTime) row[2];
            DisasterStatus status = (DisasterStatus) row[3];

            statsMap.putIfAbsent(region, new RegionStats());
            RegionStats rs = statsMap.get(region);
            rs.total++;
            
            if (status == DisasterStatus.RESOLVED && resolvedAt != null && createdAt != null) {
                rs.resolved++;
                double days = ChronoUnit.HOURS.between(createdAt, resolvedAt) / 24.0;
                rs.totalDaysForResolution += Math.max(0, days);
            }
        }

        return statsMap.entrySet().stream()
                .map(e -> {
                    String region = e.getKey();
                    RegionStats rs = e.getValue();
                    double avgDays = rs.resolved > 0 ? (rs.totalDaysForResolution / rs.resolved) : 0.0;
                    double efficiency = rs.total > 0 ? ((double) rs.resolved / rs.total) * 100.0 : 0.0;
                    return new RegionPerformanceDTO(region, avgDays, efficiency, rs.total);
                })
                .sorted(Comparator.comparing(RegionPerformanceDTO::getTotalDisasters).reversed())
                .collect(Collectors.toList());
    }

    public List<ResponderPerformanceDTO> getResponderPerformance() {
        List<Object[]> rawStats = rescueTaskRepository.getResponderCompletionStats();
        
        if (rawStats.isEmpty()) {
            return generateMockResponderPerformance();
        }

        return rawStats.stream()
                .map(row -> {
                    String name = (String) row[0];
                    Long total = ((Number) row[1]).longValue();
                    Long completed = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                    double rate = total > 0 ? ((double) completed / total) * 100.0 : 0.0;
                    return new ResponderPerformanceDTO(name, total, completed, rate);
                })
                .sorted(Comparator.comparing(ResponderPerformanceDTO::getCompletionRate).reversed())
                .collect(Collectors.toList());
    }

    public AlertEngagementDTO getAlertInsights() {
        long totalBroadcasted = alertRepository.count();
        long acknowledged = alertEngagementRepository.countAcknowledgedAlerts();
        long ignored = alertEngagementRepository.countIgnoredAlerts();

        if (totalBroadcasted == 0) {
            return new AlertEngagementDTO(150L, 120L, 30L, 80.0);
        }

        // If engagement hasn't been tracked yet, mock engagement values based on broadcast ratio
        if (acknowledged == 0 && ignored == 0) {
            acknowledged = (long)(totalBroadcasted * 0.7);
            ignored = totalBroadcasted - acknowledged;
        }

        double rate = totalBroadcasted > 0 ? ((double) acknowledged / (acknowledged + ignored)) * 100.0 : 0.0;
        return new AlertEngagementDTO(totalBroadcasted, acknowledged, ignored, rate);
    }

    // --- Mock Data Generators ---
    private List<DisasterTrendDTO> generateMockTrends() {
        return List.of(
            new DisasterTrendDTO("2023-11", 5L),
            new DisasterTrendDTO("2023-12", 8L),
            new DisasterTrendDTO("2024-01", 12L),
            new DisasterTrendDTO("2024-02", 7L),
            new DisasterTrendDTO("2024-03", 15L),
            new DisasterTrendDTO("2024-04", 10L)
        );
    }

    private List<RegionPerformanceDTO> generateMockRegionPerformance() {
        return List.of(
            new RegionPerformanceDTO("North Region", 2.5, 85.0, 45L),
            new RegionPerformanceDTO("South Region", 1.2, 95.0, 30L),
            new RegionPerformanceDTO("East Coast", 4.0, 70.0, 60L),
            new RegionPerformanceDTO("West Region", 3.1, 78.0, 50L)
        );
    }

    private List<ResponderPerformanceDTO> generateMockResponderPerformance() {
        return List.of(
            new ResponderPerformanceDTO("John Doe", 50L, 45L, 90.0),
            new ResponderPerformanceDTO("Jane Smith", 40L, 38L, 95.0),
            new ResponderPerformanceDTO("Alpha Team", 120L, 100L, 83.33),
            new ResponderPerformanceDTO("Bravo Unit", 80L, 75L, 93.75)
        );
    }

    private static class RegionStats {
        long total = 0;
        long resolved = 0;
        double totalDaysForResolution = 0.0;
    }
}
