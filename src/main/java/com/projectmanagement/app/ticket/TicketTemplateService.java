package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketTemplateService {
    private final TicketTemplateRepository repo;
    private final ProjectRepository projects;
    private final TicketTypeRepository types;
    private final TicketStatusRepository statuses;
    private final TicketPriorityRepository priorities;
    private final ProjectAccessService access;
    private final TicketService tickets;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<TicketTemplateResponse> all() {
        return repo.findAllByOrderByNameAsc().stream().map(this::out).toList();
    }

    @Transactional(readOnly = true)
    public List<TicketTemplateResponse> project(Long id) {
        Project p = projects.findById(id).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        return repo.findByProjectIdAndActiveTrueOrderByNameAsc(id).stream().map(this::out).toList();
    }

    public TicketTemplateResponse create(TicketTemplateRequest r) {
        Project p = projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        access.requireManager(p);
        return out(repo.save(TicketTemplate.builder().project(p)
                .ticketType(types.findById(r.ticketTypeId).orElseThrow(() -> nf("Ticket type not found")))
                .status(statuses.findById(r.statusId).orElseThrow(() -> nf("Status not found")))
                .priority(priorities.findById(r.priorityId).orElseThrow(() -> nf("Priority not found")))
                .name(r.name.trim()).content(r.content).estimation(r.estimation).customFieldsJson(r.customFieldsJson)
                .active(r.active == null || r.active).build()));
    }

    public TicketTemplateResponse update(Long id, TicketTemplateRequest r) {
        TicketTemplate t = repo.findById(id).orElseThrow(() -> nf("Template not found"));
        access.requireManager(t.project);
        t.setName(r.name.trim());
        t.setContent(r.content);
        t.setEstimation(r.estimation);
        t.setCustomFieldsJson(r.customFieldsJson);
        t.setActive(r.active == null || r.active);
        t.setTicketType(types.findById(r.ticketTypeId).orElseThrow(() -> nf("Ticket type not found")));
        t.setStatus(statuses.findById(r.statusId).orElseThrow(() -> nf("Status not found")));
        t.setPriority(priorities.findById(r.priorityId).orElseThrow(() -> nf("Priority not found")));
        return out(repo.save(t));
    }

    public void delete(Long id) {
        TicketTemplate t = repo.findById(id).orElseThrow(() -> nf("Template not found"));
        access.requireManager(t.project);
        t.setActive(false);
    }

    public TicketResponse apply(Long id, TicketTemplateApplyRequest r) {
        TicketTemplate t = repo.findById(id).orElseThrow(() -> nf("Template not found"));
        access.requireEditor(t.project);
        Map<String, String> cf = r.customFields == null ? read(t.customFieldsJson) : r.customFields;
        TicketRequest q = TicketRequest.builder().name(blank(r.name) ? t.name : r.name)
                .content(blank(r.content) ? t.content : r.content).ownerId(r.ownerId).responsibleId(r.responsibleId)
                .statusId(r.statusId == null ? t.status.getId() : r.statusId).projectId(t.project.getId())
                .typeId(t.ticketType.getId()).priorityId(r.priorityId == null ? t.priority.getId() : r.priorityId)
                .estimation(r.estimation == null ? t.estimation : r.estimation).customFields(cf).build();
        return tickets.create(q);
    }

    private Map<String, String> read(String s) {
        try {
            return s == null || s.isBlank() ? Map.of() : mapper.readValue(s, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid template custom fields JSON");
        }
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private TicketTemplateResponse out(TicketTemplate t) {
        return TicketTemplateResponse.builder().id(t.id).projectId(t.project.getId()).projectName(t.project.getName())
                .ticketTypeId(t.ticketType.getId()).ticketTypeName(t.ticketType.getName()).statusId(t.status.getId())
                .statusName(t.status.getName()).priorityId(t.priority.getId()).priorityName(t.priority.getName())
                .name(t.name).content(t.content).estimation(t.estimation).customFieldsJson(t.customFieldsJson)
                .active(t.active).build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
}
