package com.projectmanagement.app.release;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/releases")
public class ReleaseVersionController {
    private final ReleaseVersionService service;

    public ReleaseVersionController(ReleaseVersionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('release.view') or hasRole('ADMIN')")
    public ResponseEntity<List<ReleaseVersionResponse>> list(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.list(projectId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('release.view') or hasRole('ADMIN')")
    public ResponseEntity<ReleaseVersionResponse> get(@PathVariable Long projectId, @PathVariable Long id) {
        return ResponseEntity.ok(service.get(projectId, id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('release.create') or hasRole('ADMIN')")
    public ResponseEntity<ReleaseVersionResponse> create(@PathVariable Long projectId,
            @Valid @RequestBody ReleaseVersionRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(projectId, r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('release.update') or hasRole('ADMIN')")
    public ResponseEntity<ReleaseVersionResponse> update(@PathVariable Long projectId, @PathVariable Long id,
            @Valid @RequestBody ReleaseVersionRequest r) {
        return ResponseEntity.ok(service.update(projectId, id, r));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('release.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        service.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('release.update') or hasRole('ADMIN')")
    public ResponseEntity<ReleaseVersionResponse> status(@PathVariable Long projectId, @PathVariable Long id,
            @RequestParam ReleaseStatus status) {
        return ResponseEntity.ok(service.updateStatus(projectId, id, status));
    }

    @GetMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('release.view') or hasRole('ADMIN')")
    public ResponseEntity<ReleaseVersionProgressResponse> progress(@PathVariable Long projectId,
            @PathVariable Long id) {
        return ResponseEntity.ok(service.progress(projectId, id));
    }

    @GetMapping("/{id}/burndown")
    @PreAuthorize("hasAuthority('release.view') or hasRole('ADMIN')")
    public ResponseEntity<ReleaseBurndownResponse> burndown(@PathVariable Long projectId, @PathVariable Long id) {
        return ResponseEntity.ok(service.burndown(projectId, id));
    }

    @GetMapping("/{id}/tickets")
    @PreAuthorize("hasAuthority('release.view') or hasRole('ADMIN')")
    public ResponseEntity<List<ReleaseTicketResponse>> tickets(@PathVariable Long projectId, @PathVariable Long id) {
        return ResponseEntity.ok(service.tickets(projectId, id));
    }

    @PostMapping("/{id}/tickets/{ticketId}")
    @PreAuthorize("hasAuthority('release.update') or hasRole('ADMIN')")
    public ResponseEntity<Void> assign(@PathVariable Long projectId, @PathVariable Long id,
            @PathVariable Long ticketId) {
        service.assignTicket(projectId, id, ticketId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/tickets/{ticketId}")
    @PreAuthorize("hasAuthority('release.update') or hasRole('ADMIN')")
    public ResponseEntity<Void> remove(@PathVariable Long projectId, @PathVariable Long id,
            @PathVariable Long ticketId) {
        service.removeTicket(projectId, id, ticketId);
        return ResponseEntity.noContent().build();
    }
}
