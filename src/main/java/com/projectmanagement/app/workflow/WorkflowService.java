package com.projectmanagement.app.workflow;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketStatus;
import com.projectmanagement.app.ticket.TicketStatusRepository;
import com.projectmanagement.app.ticket.TicketType;
import com.projectmanagement.app.ticket.TicketTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowService {
    private final WorkflowRuleRepository repo;
    private final ProjectRepository projects;
    private final TicketTypeRepository types;
    private final TicketStatusRepository statuses;
    private final ProjectAccessService access;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<WorkflowRuleResponse> all() {
        return repo.findAllByOrderByIdAsc().stream().map(this::out).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkflowRuleResponse> byProject(Long id) {
        Project p = projects.findById(id).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        return repo.findByProjectIdAndActiveTrueOrderByIdAsc(id).stream().map(this::out).toList();
    }

    public WorkflowRuleResponse create(WorkflowRuleRequest r) {
        Project p = r.projectId == null ? null
                : projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        if (p != null)
            access.requireManager(p);
        TicketType t = r.ticketTypeId == null ? null
                : types.findById(r.ticketTypeId).orElseThrow(() -> nf("Ticket type not found"));
        TicketStatus from = statuses.findById(r.fromStatusId).orElseThrow(() -> nf("From status not found")),
                to = statuses.findById(r.toStatusId).orElseThrow(() -> nf("To status not found"));
        check(p, from);
        check(p, to);
        return out(repo.save(WorkflowRule.builder().project(p).ticketType(t).fromStatus(from).toStatus(to)
                .requiredPermission(blank(r.requiredPermission) ? "ticket.update" : r.requiredPermission.trim())
                .conditionJson(r.conditionJson).validatorJson(r.validatorJson).postFunctionJson(r.postFunctionJson)
                .active(r.active == null || r.active).build()));
    }

    public WorkflowRuleResponse update(Long id, WorkflowRuleRequest r) {
        WorkflowRule x = repo.findById(id).orElseThrow(() -> nf("Workflow rule not found"));
        Project p = r.projectId == null ? null
                : projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        if (p != null)
            access.requireManager(p);
        TicketStatus from = statuses.findById(r.fromStatusId).orElseThrow(() -> nf("From status not found")),
                to = statuses.findById(r.toStatusId).orElseThrow(() -> nf("To status not found"));
        check(p, from);
        check(p, to);
        x.setProject(p);
        x.setTicketType(r.ticketTypeId == null ? null
                : types.findById(r.ticketTypeId).orElseThrow(() -> nf("Ticket type not found")));
        x.setFromStatus(from);
        x.setToStatus(to);
        x.setRequiredPermission(blank(r.requiredPermission) ? "ticket.update" : r.requiredPermission.trim());
        x.setConditionJson(r.conditionJson);
        x.setValidatorJson(r.validatorJson);
        x.setPostFunctionJson(r.postFunctionJson);
        x.setActive(r.active == null || r.active);
        return out(repo.save(x));
    }

    public void delete(Long id) {
        WorkflowRule x = repo.findById(id).orElseThrow(() -> nf("Workflow rule not found"));
        x.setActive(false);
        repo.save(x);
    }

    public void validateTransition(Ticket ticket, TicketStatus from, TicketStatus to) {
        if (from == null)
            return;
        var rules = repo.findByProjectIdAndTicketTypeIdAndFromStatusIdAndToStatusIdAndActiveTrue(
                ticket.getProject().getId(), ticket.getType().getId(), from.getId(), to.getId());
        if (rules.isEmpty())
            return;
        WorkflowRule r = rules.get(0);
        Map<String, Object> c = read(r.getConditionJson()), v = read(r.getValidatorJson());
        if (Boolean.TRUE.equals(c.get("requireAssignee")) && ticket.getResponsible() == null)
            throw bad("Workflow condition: assignee is required");
        if (Boolean.TRUE.equals(c.get("requireDescription"))
                && (ticket.getContent() == null || ticket.getContent().isBlank()))
            throw bad("Workflow condition: description is required");
        if (Boolean.TRUE.equals(v.get("requireAssignee")) && ticket.getResponsible() == null)
            throw bad("Workflow validator: assignee is required");
        if (Boolean.TRUE.equals(v.get("requireDescription"))
                && (ticket.getContent() == null || ticket.getContent().isBlank()))
            throw bad("Workflow validator: description is required");
    }

    public void applyPostFunctions(Ticket ticket, String json) {
        Map<String, Object> p = read(json);
        if (Boolean.TRUE.equals(p.get("clearAssignee")))
            ticket.setResponsible(null);
        if (Boolean.TRUE.equals(p.get("clearSprint")))
            ticket.setSprint(null);
        if (Boolean.TRUE.equals(p.get("clearMilestone")))
            ticket.setMilestone(null);
    }

    public void applyTransitionPostFunctions(Ticket ticket, TicketStatus from, TicketStatus to) {
        if (from == null)
            return;
        var rules = repo.findByProjectIdAndTicketTypeIdAndFromStatusIdAndToStatusIdAndActiveTrue(
                ticket.getProject().getId(), ticket.getType().getId(), from.getId(), to.getId());
        if (!rules.isEmpty())
            applyPostFunctions(ticket, rules.get(0).getPostFunctionJson());
    }

    private Map<String, Object> read(String s) {
        try {
            return s == null || s.isBlank() ? Map.of() : mapper.readValue(s, Map.class);
        } catch (Exception e) {
            throw bad("Invalid workflow JSON configuration");
        }
    }

    private void check(Project p, TicketStatus s) {
        if (p != null && s.getProject() != null && !p.getId().equals(s.getProject().getId()))
            throw bad("Status does not belong to project");
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private WorkflowRuleResponse out(WorkflowRule x) {
        return WorkflowRuleResponse.builder().id(x.getId())
                .projectId(x.getProject() == null ? null : x.getProject().getId())
                .projectName(x.getProject() == null ? null : x.getProject().getName())
                .ticketTypeId(x.getTicketType() == null ? null : x.getTicketType().getId())
                .ticketTypeName(x.getTicketType() == null ? null : x.getTicketType().getName())
                .fromStatusId(x.getFromStatus().getId()).fromStatusName(x.getFromStatus().getName())
                .toStatusId(x.getToStatus().getId()).toStatusName(x.getToStatus().getName())
                .requiredPermission(x.getRequiredPermission()).conditionJson(x.getConditionJson())
                .validatorJson(x.getValidatorJson()).postFunctionJson(x.getPostFunctionJson()).active(x.getActive())
                .build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }

    private ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }
}
