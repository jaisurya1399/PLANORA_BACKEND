package com.projectmanagement.app.project;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/working-hours")
public class ProjectWorkingHoursController {

    private final ProjectWorkingHoursService service;

    public ProjectWorkingHoursController(ProjectWorkingHoursService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectWorkingHoursResponse>> getHistory(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(service.getHistory(projectId));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectWorkingHoursResponse> upsert(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectWorkingHoursRequest request) {
        return ResponseEntity.ok(service.upsert(projectId, request));
    }
}
