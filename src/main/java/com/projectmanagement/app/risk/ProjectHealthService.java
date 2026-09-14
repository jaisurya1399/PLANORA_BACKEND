package com.projectmanagement.app.risk;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.projectmanagement.app.dependency.DependencyRepository;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.sla.SlaService;
import com.projectmanagement.app.sla.SlaTicketResponse;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectHealthService {
    private final ProjectRepository projects;
    private final ProjectRiskRepository risks;
    private final TicketRepository tickets;
    private final DependencyRepository deps;
    private final SlaService sla;
    private final ProjectAccessService access;

    public ProjectHealthResponse health(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        List<Ticket> ts = tickets.findByProjectIdAndDeletedAtIsNull(pid);
        long overdue = ts.stream().filter(t -> t.getResolvedAt() == null && t.getCreatedAt() != null
                && t.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))).count();
        long high = risks.findByProjectIdOrderByCreatedAtDesc(pid).stream()
                .filter(r -> !"CLOSED".equalsIgnoreCase(r.getStatus())
                        && ("HIGH".equalsIgnoreCase(r.getImpact()) || "HIGH".equalsIgnoreCase(r.getProbability())))
                .count();
        long blocked = deps.findBySourceTicketProjectIdOrTargetTicketProjectId(pid, pid).stream()
                .filter(d -> "BLOCKS".equalsIgnoreCase(d.getType())).count();
        long breach = sla.evaluate(pid).stream().filter(SlaTicketResponse::isBreached).count();
        int score = 100 - (int) Math.min(45, overdue * 5) - (int) Math.min(25, high * 10)
                - (int) Math.min(15, blocked * 5) - (int) Math.min(15, breach * 5);
        String h = score >= 80 ? "HEALTHY" : score >= 60 ? "AT_RISK" : "CRITICAL";
        return ProjectHealthResponse.builder().projectId(pid).projectName(p.getName()).health(h)
                .score(Math.max(0, score)).totalTickets(ts.size()).overdueTickets(overdue).openHighRisks(high)
                .blockedDependencies(blocked).breachedSla(breach)
                .rationale("Score combines overdue work, high risks, blocked dependencies and SLA breaches.").build();
    }
}
