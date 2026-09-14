package com.projectmanagement.app.project;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectManagementController {
    private final ProjectManagementService service;

    public ProjectManagementController(ProjectManagementService service) {
        this.service = service;
    }

    @GetMapping("/{id}/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> settings(@PathVariable Long id) {
        return ResponseEntity.ok(service.settings(id));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }

    @PutMapping("/{id}/unarchive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(service.unarchive(id));
    }

    @GetMapping("/archived")
    @PreAuthorize("hasAuthority('project.view') or hasRole('ADMIN')")
    public ResponseEntity<List<ProjectResponse>> archived() {
        return ResponseEntity.ok(service.archived());
    }

    @PostMapping("/{id}/clone")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> cloneProject(@PathVariable Long id,
            @Valid @RequestBody ProjectCloneRequest request) {
        return ResponseEntity.status(201).body(service.cloneProject(id, request));
    }
}
