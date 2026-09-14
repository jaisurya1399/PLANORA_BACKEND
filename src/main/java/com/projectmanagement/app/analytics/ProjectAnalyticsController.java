package com.projectmanagement.app.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/analytics")
public class ProjectAnalyticsController {
    private final ProjectAnalyticsService service;

    public ProjectAnalyticsController(ProjectAnalyticsService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('project.view') or hasRole('ADMIN')")
    public ResponseEntity<ProjectAnalyticsResponse> get(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.get(projectId));
    }
}
