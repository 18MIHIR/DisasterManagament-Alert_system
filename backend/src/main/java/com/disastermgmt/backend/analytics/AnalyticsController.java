package com.disastermgmt.backend.analytics;

import com.disastermgmt.backend.analytics.dto.AlertEngagementDTO;
import com.disastermgmt.backend.analytics.dto.DisasterTrendDTO;
import com.disastermgmt.backend.analytics.dto.RegionPerformanceDTO;
import com.disastermgmt.backend.analytics.dto.ResponderPerformanceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/disaster-trends")
    public ResponseEntity<List<DisasterTrendDTO>> getDisasterTrends() {
        return ResponseEntity.ok(analyticsService.getDisasterTrends());
    }

    @GetMapping("/region-performance")
    public ResponseEntity<List<RegionPerformanceDTO>> getRegionPerformance() {
        return ResponseEntity.ok(analyticsService.getRegionPerformance());
    }

    @GetMapping("/responder-performance")
    public ResponseEntity<List<ResponderPerformanceDTO>> getResponderPerformance() {
        return ResponseEntity.ok(analyticsService.getResponderPerformance());
    }

    @GetMapping("/alert-insights")
    public ResponseEntity<AlertEngagementDTO> getAlertInsights() {
        return ResponseEntity.ok(analyticsService.getAlertInsights());
    }
}
