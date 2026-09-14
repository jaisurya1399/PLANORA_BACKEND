package com.projectmanagement.app.ticket;

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
@RequestMapping("/api/ticket-templates")
@RequiredArgsConstructor
public class TicketTemplateController {
    private final TicketTemplateService service;

    @GetMapping
    @PreAuthorize("hasAuthority('ticket_template.view') or hasRole('ADMIN')")
    public List<TicketTemplateResponse> all() {
        return service.all();
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('ticket_template.view') or hasRole('ADMIN')")
    public List<TicketTemplateResponse> project(@PathVariable Long projectId) {
        return service.project(projectId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ticket_template.create') or hasRole('ADMIN')")
    public ResponseEntity<TicketTemplateResponse> create(@Valid @RequestBody TicketTemplateRequest r) {
        return ResponseEntity.status(201).body(service.create(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket_template.update') or hasRole('ADMIN')")
    public TicketTemplateResponse update(@PathVariable Long id, @Valid @RequestBody TicketTemplateRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket_template.delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasAuthority('ticket.create') or hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> apply(@PathVariable Long id,
            @Valid @RequestBody TicketTemplateApplyRequest r) {
        return ResponseEntity.status(201).body(service.apply(id, r));
    }
}
