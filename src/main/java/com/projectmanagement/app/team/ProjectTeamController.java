package com.projectmanagement.app.team;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/project-teams")
@RequiredArgsConstructor
public class ProjectTeamController {
    private final ProjectTeamService service;

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('project_team.view') or hasRole('ADMIN')")
    public List<ProjectTeamResponse> list(@PathVariable Long projectId) {
        return service.list(projectId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('project_team.create') or hasRole('ADMIN')")
    public ResponseEntity<ProjectTeamResponse> create(@Valid @RequestBody ProjectTeamRequest r) {
        return ResponseEntity.status(201).body(service.create(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('project_team.update') or hasRole('ADMIN')")
    public ProjectTeamResponse update(@PathVariable Long id, @Valid @RequestBody ProjectTeamRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('project_team.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('project_team.update') or hasRole('ADMIN')")
    public ProjectTeamResponse add(@PathVariable Long id, @Valid @RequestBody ProjectTeamMemberRequest r) {
        return service.add(id, r);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('project_team.update') or hasRole('ADMIN')")
    public ProjectTeamResponse remove(@PathVariable Long id, @PathVariable Long userId) {
        return service.remove(id, userId);
    }
}
