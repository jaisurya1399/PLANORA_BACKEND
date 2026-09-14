package com.projectmanagement.app.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.sprint.SprintRepository;
import com.projectmanagement.app.sprint.SprintStatus;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketActivityRepository;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.ticket.TicketStatusCategory;

@Service
@Transactional(readOnly = true)
public class ProjectAnalyticsService {
    private final ProjectRepository projectRepository;
    private final ProjectAccessService access;
    private final TicketRepository ticketRepository;
    private final SprintRepository sprintRepository;
    private final TicketActivityRepository activityRepository;

    public ProjectAnalyticsService(ProjectRepository projectRepository, ProjectAccessService access,
            TicketRepository ticketRepository, SprintRepository sprintRepository,
            TicketActivityRepository activityRepository) {
        this.projectRepository = projectRepository;
        this.access = access;
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
        this.activityRepository = activityRepository;
    }

    public ProjectAnalyticsResponse get(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(project);
        List<Ticket> tickets = ticketRepository.findByProjectIdAndDeletedAtIsNull(projectId);
        long done = tickets.stream().filter(t -> category(t) == TicketStatusCategory.DONE).count();
        long cancelled = tickets.stream().filter(t -> category(t) == TicketStatusCategory.CANCELLED).count();
        long backlog = tickets.stream().filter(t -> category(t) == TicketStatusCategory.BACKLOG).count();
        long inProgress = tickets.stream().filter(t -> category(t) == TicketStatusCategory.IN_PROGRESS).count();
        BigDecimal totalEstimate = sum(tickets);
        BigDecimal completedEstimate = sum(
                tickets.stream().filter(t -> category(t) == TicketStatusCategory.DONE).toList());
        List<Ticket> resolved = tickets.stream().filter(t -> t.getResolvedAt() != null).toList();
        return ProjectAnalyticsResponse.builder().projectId(projectId).totalIssues(tickets.size())
                .openIssues(tickets.size() - done - cancelled)
                .completedIssues(done).cancelledIssues(cancelled).backlogIssues(backlog).inProgressIssues(inProgress)
                .totalEstimate(totalEstimate).completedEstimate(completedEstimate)
                .completionPercent(percent(completedEstimate, totalEstimate))
                .averageLeadHours(averageLead(resolved)).averageCycleHours(averageCycle(resolved))
                .workload(workload(tickets)).velocity(velocity(projectId)).build();
    }

    private TicketStatusCategory category(Ticket ticket) {
        return ticket.getStatus().getCategory();
    }

    private BigDecimal sum(List<Ticket> tickets) {
        return tickets.stream().map(Ticket::getEstimation).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    private BigDecimal percent(BigDecimal value, BigDecimal total) {
        return total.signum() == 0 ? BigDecimal.ZERO
                : value.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageLead(List<Ticket> resolved) {
        return average(
                resolved.stream().map(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes()).toList());
    }

    private BigDecimal averageCycle(List<Ticket> resolved) {
        List<Long> minutes = new ArrayList<>();
        for (Ticket ticket : resolved)
            activityRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()).stream()
                    .filter(a -> a.getNewStatus() != null
                            && a.getNewStatus().getCategory() == TicketStatusCategory.IN_PROGRESS)
                    .findFirst()
                    .ifPresent(
                            a -> minutes.add(Duration.between(a.getCreatedAt(), ticket.getResolvedAt()).toMinutes()));
        return average(minutes);
    }

    private BigDecimal average(List<Long> minutes) {
        return minutes.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(minutes.stream().mapToLong(Long::longValue).average().orElse(0) / 60d).setScale(2,
                        RoundingMode.HALF_UP);
    }

    private List<ProjectAnalyticsResponse.AssigneeWorkload> workload(List<Ticket> tickets) {
        Map<Long, List<Ticket>> byUser = new LinkedHashMap<>();
        for (Ticket ticket : tickets)
            byUser.computeIfAbsent(ticket.getResponsible() == null ? null : ticket.getResponsible().getId(),
                    key -> new ArrayList<>()).add(ticket);
        return byUser.entrySet().stream().map(e -> {
            List<Ticket> issues = e.getValue();
            Ticket first = issues.getFirst();
            long completed = issues.stream().filter(t -> category(t) == TicketStatusCategory.DONE).count();
            return ProjectAnalyticsResponse.AssigneeWorkload.builder().userId(e.getKey())
                    .userName(first.getResponsible() == null ? "Unassigned" : first.getResponsible().getName())
                    .openIssues(issues.size() - completed).completedIssues(
                            completed)
                    .openEstimate(sum(issues.stream().filter(t -> category(t) != TicketStatusCategory.DONE
                            && category(t) != TicketStatusCategory.CANCELLED).toList()))
                    .build();
        }).toList();
    }

    private List<ProjectAnalyticsResponse.SprintVelocity> velocity(Long projectId) {
        return sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.COMPLETED).stream().map(sprint -> {
            List<Ticket> issues = ticketRepository.findBySprintIdOrderByOrderAsc(sprint.getId());
            List<Ticket> done = issues.stream().filter(t -> category(t) == TicketStatusCategory.DONE).toList();
            return ProjectAnalyticsResponse.SprintVelocity.builder().sprintId(sprint.getId())
                    .sprintName(sprint.getName()).completedIssues((long) done.size()).completedEstimate(sum(done))
                    .build();
        }).toList();
    }
}
