package com.projectmanagement.app.audit;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.ticket.Ticket;

@Service
@Transactional
public class AuditService {
    private final AuditEventRepository repository;
    private final CurrentUserService currentUser;
    private final ProjectAccessService access;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository repository, CurrentUserService currentUser, ProjectAccessService access,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.currentUser = currentUser;
        this.access = access;
        this.objectMapper = objectMapper;
    }

    public void record(Project project, Ticket ticket, String eventType, String entityType, Long entityId,
            Map<String, ?> changes) {
        repository.save(AuditEvent.builder().project(project).ticket(ticket).actor(currentUser.getCurrentUser())
                .eventType(eventType).entityType(entityType).entityId(entityId).changesJson(json(changes)).build());
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> byProject(Project project) {
        access.requireManager(project);
        return repository.findByProjectIdOrderByCreatedAtDesc(project.getId()).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> byTicket(Ticket ticket) {
        access.requireView(ticket.getProject());
        return repository.findByTicketIdOrderByCreatedAtDesc(ticket.getId()).stream().map(this::response).toList();
    }

    private String json(Map<String, ?> changes) {
        try {
            return objectMapper.writeValueAsString(changes == null ? Map.of() : changes);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize audit changes", exception);
        }
    }

    private AuditEventResponse response(AuditEvent event) {
        return AuditEventResponse.builder().id(event.getId()).projectId(event.getProject().getId())
                .ticketId(event.getTicket() == null ? null : event.getTicket().getId())
                .actorId(event.getActor() == null ? null : event.getActor().getId())
                .actorName(event.getActor() == null ? null : event.getActor().getName()).eventType(event.getEventType())
                .entityType(event.getEntityType()).entityId(event.getEntityId()).changesJson(event.getChangesJson())
                .createdAt(event.getCreatedAt()).build();
    }
}
