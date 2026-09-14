package com.projectmanagement.app.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiPmService {
    private final ProjectRepository projects;
    private final TicketRepository tickets;
    private final CurrentUserService currentUser;

    public AiPmResponse analyze(Long projectId) {
        Project p = projects.findById(projectId).orElseThrow(() -> new NoSuchElementException("Project not found"));
        if (!projects.existsByIdAndUserCanAccess(projectId, currentUser.getCurrentUserId()))
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Project access denied");
        List<Ticket> ts = tickets.findByProjectIdAndDeletedAtIsNull(projectId);
        long open = ts.stream().filter(t -> t.getResolvedAt() == null).count();
        long unassigned = ts.stream().filter(t -> t.getResponsible() == null).count();
        long high = ts.stream()
                .filter(t -> t.getPriority() != null && isHigh(t.getPriority().getName()) && t.getResolvedAt() == null)
                .count();
        long stale = ts.stream().filter(t -> t.getResolvedAt() == null && t.getUpdatedAt() != null
                && t.getUpdatedAt().isBefore(java.time.LocalDateTime.now().minusDays(7))).count();
        long blocked = ts.stream().filter(t -> t.getStatus() != null && t.getStatus().getName() != null
                && t.getStatus().getName().toLowerCase().contains("block")).count();

        List<String> risks = new ArrayList<>();
        if (unassigned > 0)
            risks.add(unassigned + " open issue(s) are unassigned.");
        if (high > 0)
            risks.add(high + " high/critical priority issue(s) remain open.");
        if (stale > 0)
            risks.add(stale + " issue(s) have had no update for 7+ days.");
        if (blocked > 0)
            risks.add(blocked + " issue(s) appear blocked based on status.");
        if (risks.isEmpty())
            risks.add("No major delivery risk signal was detected from the available project data.");

        List<String> rec = new ArrayList<>();
        if (unassigned > 0)
            rec.add("Assign owners to unassigned work before the next planning cycle.");
        if (high > 0)
            rec.add("Review high-priority work and create a focused execution plan.");
        if (stale > 0)
            rec.add("Review stale issues and either update, re-scope, or close them.");
        if (blocked > 0)
            rec.add("Resolve blockers and record dependencies/impediments explicitly.");
        rec.add("Use sprint capacity and current backlog size to keep committed work within realistic team capacity.");

        List<String> actions = ts.stream().filter(t -> t.getResolvedAt() == null)
                .sorted(Comparator.comparing(Ticket::getUpdatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .limit(5).map(t -> t.getCode() + ": review next action for '" + t.getName() + "'")
                .collect(Collectors.toList());
        String summary = "Project '" + p.getName() + "' has " + open + " open issue(s), " + high
                + " high-priority open issue(s), " + unassigned + " unassigned issue(s), and " + stale
                + " stale issue(s).";
        return AiPmResponse.builder().summary(summary).risks(risks).recommendations(rec).nextActions(actions).build();
    }

    private boolean isHigh(String n) {
        String x = n.toLowerCase();
        return x.contains("high") || x.contains("critical") || x.contains("urgent");
    }
}
