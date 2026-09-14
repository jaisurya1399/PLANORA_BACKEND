package com.projectmanagement.app.sla;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SlaService {
    private final SlaPolicyRepository policies;
    private final ProjectRepository projects;
    private final TicketRepository tickets;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public List<SlaPolicy> policies(Long projectId) {
        Project p = projects.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        return policies.findByProjectId(projectId);
    }

    public SlaPolicy save(SlaPolicyRequest x) {
        Project p = projects.findById(x.getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireManager(p);
        return policies.save(SlaPolicy.builder().project(p).name(x.getName()).targetHours(x.getTargetHours())
                .priorityFilter(x.getPriorityFilter()).enabled(x.getEnabled() == null || x.getEnabled()).build());
    }

    @Transactional(readOnly = true)
    public List<SlaTicketResponse> evaluate(Long projectId) {
        Project p = projects.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        List<SlaPolicy> ps = policies.findByProjectId(projectId).stream().filter(SlaPolicy::isEnabled).toList();
        if (ps.isEmpty())
            return List.of();
        List<SlaTicketResponse> out = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Ticket t : tickets.findByProjectIdAndDeletedAtIsNull(projectId)) {
            SlaPolicy pol = ps.stream()
                    .filter(a -> a.getPriorityFilter() == null || a.getPriorityFilter().isBlank()
                            || a.getPriorityFilter().equalsIgnoreCase(t.getPriority().getName()))
                    .findFirst().orElse(ps.get(0));
            LocalDateTime start = t.getCreatedAt();
            LocalDateTime due = start.plusHours(pol.getTargetHours());
            boolean resolved = t.getResolvedAt() != null;
            boolean breach = !resolved && now.isAfter(due);
            long remaining = resolved ? 0 : Duration.between(now, due).toMinutes();
            out.add(SlaTicketResponse.builder().ticketId(t.getId()).ticketCode(t.getCode())
                    .priority(t.getPriority().getName()).startedAt(start).dueAt(due).breached(breach).resolved(resolved)
                    .remainingMinutes(remaining).build());
        }
        return out;
    }
}
