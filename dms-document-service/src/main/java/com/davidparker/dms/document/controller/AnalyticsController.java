package com.davidparker.dms.document.controller;

import com.davidparker.dms.document.dto.DashboardAnalytics;
import com.davidparker.dms.document.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAnalytics> getDashboardAnalytics(
            @RequestParam(required = false) UUID applicationId) {
        return ResponseEntity.ok(analyticsService.getDashboardAnalytics(applicationId));
    }
}
