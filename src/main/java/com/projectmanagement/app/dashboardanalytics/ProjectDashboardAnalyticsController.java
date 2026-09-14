package com.projectmanagement.app.dashboardanalytics;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProjectDashboardAnalyticsController {
    private final ProjectDashboardAnalyticsService service;

    @GetMapping("/{projectId}/dashboard-analytics")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long projectId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.getAnalytics(projectId, days));
    }
    @GetMapping("/{projectId}/member-analytics/{userId}")
    public ResponseEntity<Map<String, Object>> getMember(@PathVariable Long projectId,
            @PathVariable Long userId, @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.getMemberAnalytics(projectId, userId, days));
    }
}
