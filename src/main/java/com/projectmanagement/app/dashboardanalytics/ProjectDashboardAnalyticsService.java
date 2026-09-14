package com.projectmanagement.app.dashboardanalytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.board.BoardStatusHistory;
import com.projectmanagement.app.board.BoardStatusHistoryRepository;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.sprint.Sprint;
import com.projectmanagement.app.sprint.SprintIssueSnapshot;
import com.projectmanagement.app.sprint.SprintIssueSnapshotRepository;
import com.projectmanagement.app.sprint.SprintRepository;
import com.projectmanagement.app.sprint.SprintStatus;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.ticket.TicketStatusCategory;

@Service
@Transactional(readOnly = true)
public class ProjectDashboardAnalyticsService {
    private final ProjectRepository projectRepository;
    private final ProjectAccessService accessService;
    private final TicketRepository ticketRepository;
    private final SprintRepository sprintRepository;
    private final SprintIssueSnapshotRepository snapshotRepository;
    private final BoardStatusHistoryRepository historyRepository;

    public ProjectDashboardAnalyticsService(ProjectRepository projectRepository, ProjectAccessService accessService,
            TicketRepository ticketRepository, SprintRepository sprintRepository,
            SprintIssueSnapshotRepository snapshotRepository,
            BoardStatusHistoryRepository historyRepository) {
        this.projectRepository = projectRepository;
        this.accessService = accessService;
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
        this.snapshotRepository = snapshotRepository;
        this.historyRepository = historyRepository;
    }

    public Map<String, Object> getAnalytics(Long projectId, int days) {
        if (days != 14 && days != 30 && days != 60 && days != 90)
            days = 30;
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        accessService.requireView(project);
        List<Ticket> tickets = ticketRepository.findByProjectIdAndDeletedAtIsNull(projectId);
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        List<BoardStatusHistory> histories = historyRepository.findByProjectIdAndChangedAtBetweenOrderByChangedAtAsc(
                projectId, LocalDateTime.now().minusDays(days), LocalDateTime.now());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("project", Map.of("id", project.getId(), "name", project.getName()));
        out.put("velocity", velocity(sprints));
        out.put("sprintReport", sprintReports(sprints, tickets));
        out.put("cumulativeFlow", cumulativeFlow(tickets, histories, days));
        out.put("controlChart", controlChart(tickets, histories));
        out.put("leadCycleTime", leadCycle(tickets, histories));
        out.put("createdResolved", createdResolved(tickets, days));
        out.put("statusDistribution", statusDistribution(tickets));
        out.put("priorityDistribution", priorityDistribution(tickets));
        out.put("assigneeWorkload", assigneeWorkload(tickets));
        out.put("issueAging", issueAging(tickets));
        out.put("estimationSummary", estimationSummary(tickets));
        return out;
    }

    private List<Map<String, Object>> velocity(List<Sprint> sprints) {
        List<Map<String, Object>> result = new ArrayList<>();
        sprints.stream().filter(s -> s.getStatus() == SprintStatus.COMPLETED)
                .sorted(Comparator.comparing(Sprint::getEndDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(12).forEach(s -> {
                    List<SprintIssueSnapshot> snaps = snapshotRepository.findBySprintIdOrderByTicketIdAsc(s.getId());
                    BigDecimal committed = snaps.stream().map(SprintIssueSnapshot::getEstimation)
                            .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal completed = snaps.stream()
                            .filter(x -> x.getFinalStatusCategory() == TicketStatusCategory.DONE)
                            .map(SprintIssueSnapshot::getEstimation).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.add(row("sprint", s.getName(), "committed", committed, "completed", completed));
                });
        return result;
    }

    private List<Map<String, Object>> sprintReports(List<Sprint> sprints, List<Ticket> tickets) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Sprint s : sprints.stream().limit(12).toList()) {
            List<Ticket> st = tickets.stream()
                    .filter(t -> t.getSprint() != null && s.getId().equals(t.getSprint().getId())).toList();
            BigDecimal total = st.stream().map(Ticket::getEstimation).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
            BigDecimal done = st.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus().getCategory() == TicketStatusCategory.DONE)
                    .map(Ticket::getEstimation).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(row("sprint", s.getName(), "issues", st.size(), "estimated", total, "completed", done));
        }
        return result;
    }

    private List<Map<String, Object>> cumulativeFlow(List<Ticket> tickets, List<BoardStatusHistory> histories,
            int days) {
        Map<String, TicketStatusCategory> current = new HashMap<>();
        for (Ticket t : tickets)
            current.put(t.getId().toString(), category(t));
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (TicketStatusCategory c : TicketStatusCategory.values())
                counts.put(c.name(), 0);
            for (Ticket t : tickets) {
                TicketStatusCategory c = categoryAt(t, histories, d.plusDays(1).atStartOfDay());
                counts.put(c.name(), counts.get(c.name()) + 1);
            }
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("date", d.toString());
            r.putAll(counts);
            result.add(r);
        }
        return result;
    }

    private TicketStatusCategory categoryAt(Ticket t, List<BoardStatusHistory> histories, LocalDateTime at) {
        TicketStatusCategory c = category(t);
        List<BoardStatusHistory> hs = histories.stream()
                .filter(h -> h.getTicket().getId().equals(t.getId()) && h.getChangedAt().isAfter(at)).toList();
        if (!hs.isEmpty()) {
            List<BoardStatusHistory> all = historyRepository.findByTicketIdOrderByChangedAtAsc(t.getId());
            for (BoardStatusHistory h : all) {
                if (h.getChangedAt().isAfter(at))
                    break;
                c = h.getToStatus().getCategory();
            }
        }
        return c;
    }

    private List<Map<String, Object>> controlChart(List<Ticket> tickets, List<BoardStatusHistory> histories) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Ticket t : tickets) {
            List<BoardStatusHistory> hs = historyRepository.findByTicketIdOrderByChangedAtAsc(t.getId());
            LocalDateTime start = hs.stream()
                    .filter(h -> h.getToStatus().getCategory() == TicketStatusCategory.IN_PROGRESS)
                    .map(BoardStatusHistory::getChangedAt).findFirst().orElse(null);
            LocalDateTime done = hs.stream().filter(h -> h.getToStatus().getCategory() == TicketStatusCategory.DONE)
                    .map(BoardStatusHistory::getChangedAt).findFirst().orElse(null);
            if (start != null && done != null && !done.isBefore(start))
                out.add(row("ticket", t.getCode(), "cycleHours",
                        round(Duration.between(start, done).toMinutes() / 60.0), "date",
                        done.toLocalDate().toString()));
        }
        return out.stream().sorted(Comparator.comparing(x -> String.valueOf(x.get("date")))).limit(200).toList();
    }

    private List<Map<String, Object>> leadCycle(List<Ticket> tickets, List<BoardStatusHistory> histories) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Ticket t : tickets) {
            LocalDateTime resolved = t.getResolvedAt();
            if (resolved == null)
                continue;
            double lead = Duration.between(t.getCreatedAt(), resolved).toMinutes() / 60.0;
            List<BoardStatusHistory> hs = historyRepository.findByTicketIdOrderByChangedAtAsc(t.getId());
            LocalDateTime start = hs.stream()
                    .filter(h -> h.getToStatus().getCategory() == TicketStatusCategory.IN_PROGRESS)
                    .map(BoardStatusHistory::getChangedAt).findFirst().orElse(null);
            Double cycle = start == null ? null : Duration.between(start, resolved).toMinutes() / 60.0;
            out.add(row("ticket", t.getCode(), "leadHours", round(lead), "cycleHours",
                    cycle == null ? null : round(cycle)));
        }
        return out;
    }

    private List<Map<String, Object>> createdResolved(List<Ticket> tickets, int days) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long c = tickets.stream().filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(d))
                    .count();
            long r = tickets.stream()
                    .filter(t -> t.getResolvedAt() != null && t.getResolvedAt().toLocalDate().equals(d)).count();
            out.add(row("date", d.toString(), "created", c, "resolved", r));
        }
        return out;
    }

    private List<Map<String, Object>> statusDistribution(List<Ticket> tickets) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Ticket t : tickets) {
            String k = t.getStatus() == null ? "UNSPECIFIED" : t.getStatus().getName();
            counts.merge(k, 1L, Long::sum);
        }
        return counts.entrySet().stream().map(e -> row("status", e.getKey(), "count", e.getValue())).toList();
    }

    private List<Map<String, Object>> priorityDistribution(List<Ticket> tickets) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Ticket t : tickets) {
            String k = t.getPriority() == null ? "UNSPECIFIED" : t.getPriority().getName();
            counts.merge(k, 1L, Long::sum);
        }
        return counts.entrySet().stream().map(e -> row("priority", e.getKey(), "count", e.getValue())).toList();
    }

    private List<Map<String, Object>> assigneeWorkload(List<Ticket> tickets) {
        Map<String, List<Ticket>> grouped = new LinkedHashMap<>();
        for (Ticket t : tickets) {
            String k = t.getResponsible() == null ? "Unassigned" : t.getResponsible().getName();
            grouped.computeIfAbsent(k, x -> new ArrayList<>()).add(t);
        }
        return grouped.entrySet().stream().map(e -> {
            BigDecimal estimate = e.getValue().stream().map(Ticket::getEstimation).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long done = e.getValue().stream()
                    .filter(t -> t.getStatus() != null && t.getStatus().getCategory() == TicketStatusCategory.DONE)
                    .count();
            return row("assignee", e.getKey(), "userId",
                    e.getValue().getFirst().getResponsible() == null ? null
                            : e.getValue().getFirst().getResponsible().getId(),
                    "issues", e.getValue().size(), "completed", done, "estimated", estimate);
        }).toList();
    }

    private List<Map<String, Object>> issueAging(List<Ticket> tickets) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> buckets = new LinkedHashMap<>();
        buckets.put("0-7 days", 0L);
        buckets.put("8-30 days", 0L);
        buckets.put("31-60 days", 0L);
        buckets.put("61-90 days", 0L);
        buckets.put("90+ days", 0L);
        for (Ticket t : tickets) {
            if (t.getResolvedAt() != null || t.getCreatedAt() == null)
                continue;
            long age = Duration.between(t.getCreatedAt(), now).toDays();
            String b = age <= 7 ? "0-7 days"
                    : age <= 30 ? "8-30 days" : age <= 60 ? "31-60 days" : age <= 90 ? "61-90 days" : "90+ days";
            buckets.put(b, buckets.get(b) + 1);
        }
        return buckets.entrySet().stream().map(e -> row("bucket", e.getKey(), "count", e.getValue())).toList();
    }

    private Map<String, Object> estimationSummary(List<Ticket> tickets) {
        BigDecimal total = tickets.stream().map(Ticket::getEstimation).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal done = tickets.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().getCategory() == TicketStatusCategory.DONE)
                .map(Ticket::getEstimation).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return row("totalEstimated", total, "completedEstimated", done, "remainingEstimated", total.subtract(done),
                "issueCount", tickets.size());
    }

    private TicketStatusCategory category(Ticket t) {
        return t.getStatus() == null || t.getStatus().getCategory() == null ? TicketStatusCategory.BACKLOG
                : t.getStatus().getCategory();
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2)
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    public Map<String, Object> getMemberAnalytics(Long projectId, Long userId, int days) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        accessService.requireView(project);
        if (days < 1)
            days = 30;

        List<Ticket> projectTickets = ticketRepository.findByProjectIdAndDeletedAtIsNull(projectId);
        List<Ticket> mine = projectTickets.stream()
                .filter(t -> t.getResponsible() != null && userId.equals(t.getResponsible().getId()))
                .toList();

        long assigned = mine.size();
        long completed = mine.stream().filter(t -> category(t) == TicketStatusCategory.DONE).count();
        long cancelled = mine.stream().filter(t -> category(t) == TicketStatusCategory.CANCELLED).count();
        long open = assigned - completed - cancelled;
        long overdue = mine.stream().filter(this::isOverdue).count();
        BigDecimal estimated = mine.stream().map(Ticket::getEstimation).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal completedEstimate = mine.stream().filter(t -> category(t) == TicketStatusCategory.DONE)
                .map(Ticket::getEstimation).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal completionRate = assigned == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed * 100.0 / assigned).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("project", Map.of("id", project.getId(), "name", project.getName()));
        out.put("member",
                Map.of("id", userId, "name", mine.isEmpty() ? "Member" : mine.getFirst().getResponsible().getName()));
        out.put("summary", row("assigned", assigned, "completed", completed, "open", open, "overdue", overdue,
                "cancelled", cancelled, "estimated", estimated, "completedEstimate", completedEstimate,
                "completionRate", completionRate));
        out.put("statusDistribution", memberStatus(mine));
        out.put("priorityDistribution", memberPriority(mine));
        out.put("sprintPerformance", memberSprints(mine));
        out.put("createdCompletedTrend", memberTrend(mine, days));
        out.put("leadCycleTime", memberLeadCycle(mine));
        return out;
    }

    private boolean isOverdue(Ticket t) {
        if (category(t) == TicketStatusCategory.DONE || category(t) == TicketStatusCategory.CANCELLED)
            return false;
        return t.getDueDate() != null && t.getDueDate().isBefore(java.time.LocalDateTime.now());
    }

    private List<Map<String, Object>> memberStatus(List<Ticket> tickets) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Ticket t : tickets) {
            String k = t.getStatus() == null ? "UNSPECIFIED" : t.getStatus().getName();
            counts.merge(k, 1L, Long::sum);
        }
        return counts.entrySet().stream().map(e -> row("status", e.getKey(), "count", e.getValue())).toList();
    }

    private List<Map<String, Object>> memberPriority(List<Ticket> tickets) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Ticket t : tickets) {
            String k = t.getPriority() == null ? "UNSPECIFIED" : t.getPriority().getName();
            counts.merge(k, 1L, Long::sum);
        }
        return counts.entrySet().stream().map(e -> row("priority", e.getKey(), "count", e.getValue())).toList();
    }

    private List<Map<String, Object>> memberSprints(List<Ticket> tickets) {
        Map<Long, List<Ticket>> grouped = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();
        for (Ticket t : tickets) {
            if (t.getSprint() == null)
                continue;
            Long id = t.getSprint().getId();
            grouped.computeIfAbsent(id, x -> new ArrayList<>()).add(t);
            names.put(id, t.getSprint().getName());
        }
        return grouped.entrySet().stream().map(e -> {
            List<Ticket> list = e.getValue();
            long done = list.stream().filter(t -> category(t) == TicketStatusCategory.DONE).count();
            BigDecimal estimate = list.stream().map(Ticket::getEstimation).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return row("sprint", names.get(e.getKey()), "assigned", list.size(), "completed", done, "estimated",
                    estimate);
        }).toList();
    }

    private List<Map<String, Object>> memberTrend(List<Ticket> tickets, int days) {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            long created = tickets.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(d)).count();
            long completed = tickets.stream()
                    .filter(t -> t.getResolvedAt() != null && t.getResolvedAt().toLocalDate().equals(d)).count();
            out.add(row("date", d.toString(), "created", created, "completed", completed));
        }
        return out;
    }

    private List<Map<String, Object>> memberLeadCycle(List<Ticket> tickets) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t.getResolvedAt() == null || t.getCreatedAt() == null)
                continue;
            double lead = java.time.Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes() / 60.0;
            out.add(row("ticket", t.getCode(), "leadHours", round(lead)));
        }
        return out;
    }

}
