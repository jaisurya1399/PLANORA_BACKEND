package com.projectmanagement.app.enterprisereport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectmanagement.app.dependency.DependencyRepository;
import com.projectmanagement.app.portfolio.Portfolio;
import com.projectmanagement.app.portfolio.PortfolioProject;
import com.projectmanagement.app.portfolio.PortfolioProjectRepository;
import com.projectmanagement.app.portfolio.PortfolioRepository;
import com.projectmanagement.app.portfolio.PortfolioService;
import com.projectmanagement.app.risk.ProjectHealthResponse;
import com.projectmanagement.app.risk.ProjectHealthService;
import com.projectmanagement.app.risk.ProjectRiskRepository;
import com.projectmanagement.app.sla.SlaService;
import com.projectmanagement.app.sla.SlaTicketResponse;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/enterprise-reports")
@RequiredArgsConstructor
public class EnterpriseReportController {
    private final EnterpriseReportService s;
    private final PortfolioService portfolios;
    private final ProjectRiskRepository risks;
    private final TicketRepository tickets;
    private final DependencyRepository deps;
    private final SlaService sla;
    private final ProjectHealthService health;
    private final PortfolioRepository portfolioRepo;
    private final PortfolioProjectRepository links;

    @GetMapping("/project/{projectId}")
    public EnterpriseReportResponse report(@PathVariable Long projectId) {
        return s.report(projectId);
    }

    @GetMapping("/portfolio/{portfolioId}")
    public EnterprisePortfolioReportResponse portfolio(@PathVariable Long portfolioId) {
        Portfolio p = portfolioRepo.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        List<PortfolioProject> ps = links.findByPortfolioId(portfolioId);
        long total = 0, resolved = 0, overdue = 0, high = 0, breach = 0, blocked = 0;
        Map<String, Integer> ph = new LinkedHashMap<>();
        for (PortfolioProject x : ps) {
            Long id = x.getProject().getId();
            List<Ticket> ts = tickets.findByProjectIdAndDeletedAtIsNull(id);
            total += ts.size();
            resolved += ts.stream().filter(t -> t.getResolvedAt() != null).count();
            overdue += ts.stream().filter(t -> t.getResolvedAt() == null && t.getCreatedAt() != null
                    && t.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusDays(7))).count();
            high += risks.findByProjectIdOrderByCreatedAtDesc(id).stream()
                    .filter(r -> !"CLOSED".equalsIgnoreCase(r.getStatus()) && (("HIGH".equalsIgnoreCase(r.getImpact()))
                            || ("HIGH".equalsIgnoreCase(r.getProbability()))))
                    .count();
            breach += sla.evaluate(id).stream().filter(SlaTicketResponse::isBreached).count();
            blocked += deps.findBySourceTicketProjectIdOrTargetTicketProjectId(id, id).stream()
                    .filter(d -> "BLOCKS".equalsIgnoreCase(d.getType())).count();
            ProjectHealthResponse h = health.health(id);
            ph.put(x.getProject().getName(), h.getScore());
        }
        return EnterprisePortfolioReportResponse.builder().portfolioId(p.getId()).portfolioName(p.getName())
                .projects(ps.size()).totalIssues(total).resolvedIssues(resolved).overdueIssues(overdue).highRisks(high)
                .slaBreaches(breach).blockedDependencies(blocked).projectHealth(ph).build();
    }
}
