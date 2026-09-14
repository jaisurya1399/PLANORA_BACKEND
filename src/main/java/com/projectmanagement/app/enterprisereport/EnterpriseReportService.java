package com.projectmanagement.app.enterprisereport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.projectmanagement.app.dependency.DependencyRepository;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.risk.ProjectHealthResponse;
import com.projectmanagement.app.risk.ProjectHealthService;
import com.projectmanagement.app.risk.ProjectRiskRepository;
import com.projectmanagement.app.sla.SlaService;
import com.projectmanagement.app.sla.SlaTicketResponse;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnterpriseReportService {
    private final ProjectRepository projects;
    private final TicketRepository tickets;
    private final ProjectRiskRepository risks;
    private final DependencyRepository deps;
    private final SlaService sla;
    private final ProjectHealthService health;
    private final ProjectAccessService access;

    public EnterpriseReportResponse report(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        List<Ticket> ts = tickets.findByProjectIdAndDeletedAtIsNull(pid);
        Map<String, Long> statuses = ts.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().getName(), Collectors.counting()));
        Map<String, Long> priorities = ts.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().getName(), Collectors.counting()));
        Map<String, Long> assignees = ts.stream().collect(Collectors.groupingBy(
                t -> t.getResponsible() == null ? "Unassigned" : t.getResponsible().getName(), Collectors.counting()));
        long resolved = ts.stream().filter(t -> t.getResolvedAt() != null).count();
        long overdue = ts.stream().filter(t -> t.getResolvedAt() == null && t.getCreatedAt() != null
                && t.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))).count();
        long high = risks.findByProjectIdOrderByCreatedAtDesc(pid).stream()
                .filter(r -> !"CLOSED".equalsIgnoreCase(r.getStatus())
                        && ("HIGH".equalsIgnoreCase(r.getImpact()) || "HIGH".equalsIgnoreCase(r.getProbability())))
                .count();
        long blocked = deps.findBySourceTicketProjectIdOrTargetTicketProjectId(pid, pid).stream()
                .filter(d -> "BLOCKS".equalsIgnoreCase(d.getType())).count();
        long breaches = sla.evaluate(pid).stream().filter(SlaTicketResponse::isBreached).count();
        ProjectHealthResponse h = health.health(pid);
        return EnterpriseReportResponse.builder().projectId(pid).projectName(p.getName()).totalIssues(ts.size())
                .openIssues(ts.size() - resolved).resolvedIssues(resolved).overdueIssues(overdue).highRisks(high)
                .blockedDependencies(blocked).slaBreaches(breaches).healthScore(h.getScore()).health(h.getHealth())
                .statusDistribution(statuses).priorityDistribution(priorities).assigneeWorkload(assignees).build();
    }
}
