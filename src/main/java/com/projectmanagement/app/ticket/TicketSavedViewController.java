package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/projects/{projectId}/ticket-views")
public class TicketSavedViewController {
    private final TicketSavedViewService service;

    public TicketSavedViewController(TicketSavedViewService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public List<TicketSavedViewResponse> list(@PathVariable Long projectId) {
        return service.list(projectId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public ResponseEntity<TicketSavedViewResponse> create(@PathVariable Long projectId,
            @Valid @RequestBody TicketSavedViewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(projectId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public TicketSavedViewResponse update(@PathVariable Long projectId, @PathVariable Long id,
            @Valid @RequestBody TicketSavedViewRequest request) {
        return service.update(projectId, id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        service.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }
}
