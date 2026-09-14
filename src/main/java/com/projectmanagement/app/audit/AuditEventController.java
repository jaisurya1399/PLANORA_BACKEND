package com.projectmanagement.app.audit;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.TicketRepository;

@RestController
@RequestMapping("/api")
public class AuditEventController {
    private final AuditService service;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;

    public AuditEventController(AuditService service, ProjectRepository projectRepository,
            TicketRepository ticketRepository) {
        this.service = service;
        this.projectRepository = projectRepository;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/projects/{projectId}/audit-events")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AuditEventResponse>> byProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.byProject(
                projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"))));
    }

    @GetMapping("/tickets/{ticketId}/audit-events")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditEventResponse>> byTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.byTicket(
                ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"))));
    }
}
