package com.projectmanagement.app.board;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.sprint.SprintRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.ticket.TicketStatus;
import com.projectmanagement.app.ticket.TicketStatusRepository;

@Service
@Transactional
public class BoardService {
    private final ProjectRepository projectRepository;
    private final ProjectAccessService access;
    private final BoardConfigRepository configRepo;
    private final BoardColumnRepository columnRepo;
    private final BoardStatusHistoryRepository historyRepo;
    private final TicketStatusRepository statusRepo;
    private final TicketRepository ticketRepo;
    private final SprintRepository sprintRepo;

    public BoardService(ProjectRepository projectRepository, ProjectAccessService access,
            BoardConfigRepository configRepo, BoardColumnRepository columnRepo,
            BoardStatusHistoryRepository historyRepo, TicketStatusRepository statusRepo, TicketRepository ticketRepo,
            SprintRepository sprintRepo) {
        this.projectRepository = projectRepository;
        this.access = access;
        this.configRepo = configRepo;
        this.columnRepo = columnRepo;
        this.historyRepo = historyRepo;
        this.statusRepo = statusRepo;
        this.ticketRepo = ticketRepo;
        this.sprintRepo = sprintRepo;
    }

    private Project project(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    public BoardConfigResponse getConfig(Long projectId) {
        Project p = project(projectId);
        access.requireView(p);
        BoardConfig c = configRepo.findByProjectId(projectId)
                .orElseGet(() -> configRepo.save(BoardConfig.builder().project(p).build()));
        return map(c);
    }

    public BoardConfigResponse saveConfig(Long projectId, BoardConfigRequest r) {
        Project p = project(projectId);
        access.requireManager(p);
        BoardConfig c = configRepo.findByProjectId(projectId).orElseGet(() -> BoardConfig.builder().project(p).build());
        c.setSwimlaneType(r.getSwimlaneType() == null ? BoardSwimlaneType.NONE : r.getSwimlaneType());
        c.setEnforceWip(Boolean.TRUE.equals(r.getEnforceWip()));
        c.setActiveSprintOnly(Boolean.TRUE.equals(r.getActiveSprintOnly()));
        c.setShowEpic(Boolean.TRUE.equals(r.getShowEpic()));
        return map(configRepo.save(c));
    }

    public List<BoardColumnConfigResponse> getColumns(Long projectId) {
        Project p = project(projectId);
        access.requireView(p);
        ensureColumns(p);
        Map<Long, Integer> counts = ticketRepo.findByProjectIdAndDeletedAtIsNull(projectId).stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().getId(), Collectors.summingInt(t -> 1)));
        return columnRepo.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId).stream()
                .map(c -> mapColumn(c, counts.getOrDefault(c.getStatus().getId(), 0))).toList();
    }

    public List<BoardColumnConfigResponse> saveColumns(Long projectId, List<BoardColumnRequest> requests) {
        Project p = project(projectId);
        access.requireManager(p);
        if (requests == null)
            throw new RuntimeException("Column configuration is required");
        Set<Long> seen = new HashSet<>();
        List<BoardColumn> saved = new ArrayList<>();
        for (BoardColumnRequest r : requests) {
            if (!seen.add(r.getStatusId()))
                throw new RuntimeException("Duplicate status in board columns: " + r.getStatusId());
            TicketStatus s = statusRepo.findById(r.getStatusId())
                    .orElseThrow(() -> new RuntimeException("Ticket status not found: " + r.getStatusId()));
            if (s.getProject() != null && !projectId.equals(s.getProject().getId()))
                throw new RuntimeException("Status does not belong to project");
            BoardColumn c = columnRepo.findByProjectIdAndStatusId(projectId, r.getStatusId())
                    .orElse(BoardColumn.builder().project(p).status(s).build());
            c.setDisplayName(r.getDisplayName() == null || r.getDisplayName().isBlank() ? s.getName()
                    : r.getDisplayName().trim());
            c.setDisplayOrder(r.getDisplayOrder());
            c.setEnabled(Boolean.TRUE.equals(r.getEnabled()));
            c.setWipLimit(r.getWipLimit() != null && r.getWipLimit() > 0 ? r.getWipLimit() : null);
            saved.add(columnRepo.save(c));
        }
        return getColumns(projectId);
    }

    public List<BoardStatusHistory> history(Long projectId, int days) {
        Project p = project(projectId);
        access.requireView(p);
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(Math.max(1, Math.min(days, 365)));
        return historyRepo.findByProjectIdAndChangedAtBetweenOrderByChangedAtAsc(projectId, from, to);
    }

    public void recordTransition(Ticket ticket, TicketStatus from, TicketStatus to) {
        if (ticket == null || ticket.getProject() == null || to == null)
            return;
        if (from != null && Objects.equals(from.getId(), to.getId()))
            return;
        historyRepo.save(BoardStatusHistory.builder().project(ticket.getProject()).ticket(ticket).fromStatus(from)
                .toStatus(to).changedAt(LocalDateTime.now()).build());
    }

    public void enforceWip(Ticket ticket, TicketStatus target) {
        if (ticket == null || target == null)
            return;
        BoardConfig cfg = configRepo.findByProjectId(ticket.getProject().getId()).orElse(null);
        if (cfg == null || !Boolean.TRUE.equals(cfg.getEnforceWip()))
            return;
        BoardColumn col = columnRepo.findByProjectIdAndStatusId(ticket.getProject().getId(), target.getId())
                .orElse(null);
        if (col == null || col.getWipLimit() == null || col.getWipLimit() <= 0)
            return;
        long count = ticketRepo
                .findByProjectIdAndStatusIdAndDeletedAtIsNull(ticket.getProject().getId(), target.getId()).stream()
                .filter(t -> !t.getId().equals(ticket.getId())).count();
        if (count >= col.getWipLimit())
            throw new RuntimeException("WIP limit reached for board column '" + col.getDisplayName() + "' (limit: "
                    + col.getWipLimit() + ")");
    }

    @Transactional(readOnly = true)
    public List<CumulativeFlowPointResponse> cumulativeFlow(Long projectId, Integer days) {
        Project p = project(projectId);
        access.requireView(p);
        int d = days == null ? 30 : Math.max(7, Math.min(days, 180));
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(d - 1).atStartOfDay(), to = LocalDateTime.now();
        List<BoardColumn> cols = columnRepo.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId).stream()
                .filter(c -> Boolean.TRUE.equals(c.getEnabled())).toList();
        if (cols.isEmpty()) {
            ensureColumns(p);
            cols = columnRepo.findByProjectIdOrderByDisplayOrderAscIdAsc(projectId).stream()
                    .filter(c -> Boolean.TRUE.equals(c.getEnabled())).toList();
        }
        Set<Long> statusIds = cols.stream().map(c -> c.getStatus().getId()).collect(Collectors.toSet());
        List<Ticket> tickets = ticketRepo.findByProjectIdAndDeletedAtIsNull(projectId);
        List<BoardStatusHistory> events = historyRepo.findByProjectIdAndChangedAtBetweenOrderByChangedAtAsc(projectId,
                from.minusDays(1), to);
        Map<Long, List<BoardStatusHistory>> byTicket = events.stream()
                .collect(Collectors.groupingBy(e -> e.getTicket().getId()));
        List<CumulativeFlowPointResponse> out = new ArrayList<>();
        for (int i = 0; i < d; i++) {
            LocalDate day = from.toLocalDate().plusDays(i);
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            Map<Long, Integer> counts = new LinkedHashMap<>();
            cols.forEach(c -> counts.put(c.getStatus().getId(), 0));
            for (Ticket t : tickets) {
                TicketStatus status = t.getStatus();
                List<BoardStatusHistory> hs = byTicket.getOrDefault(t.getId(), List.of());
                for (BoardStatusHistory h : hs) {
                    if (h.getChangedAt().isBefore(end))
                        status = h.getToStatus();
                    else
                        break;
                }
                if (status != null && statusIds.contains(status.getId()))
                    counts.compute(status.getId(), (k, v) -> v + 1);
            }
            out.add(CumulativeFlowPointResponse.builder().date(day).counts(counts).build());
        }
        return out;
    }

    private void ensureColumns(Project p) {
        List<BoardColumn> existing = columnRepo.findByProjectIdOrderByDisplayOrderAscIdAsc(p.getId());
        Set<Long> existingStatusIds = existing.stream().map(c -> c.getStatus().getId()).collect(Collectors.toSet());
        List<TicketStatus> statuses = new ArrayList<>(
                statusRepo.findByProjectIdAndDeletedAtIsNullOrderByOrderAsc(p.getId()));
        statuses.addAll(statusRepo.findByProjectIdIsNullAndDeletedAtIsNull());
        int next = existing.stream().map(BoardColumn::getDisplayOrder).filter(Objects::nonNull).max(Integer::compareTo)
                .orElse(-1) + 1;
        for (TicketStatus s : statuses.stream()
                .sorted(Comparator.comparing(TicketStatus::getOrder).thenComparing(TicketStatus::getId)).toList()) {
            if (existingStatusIds.add(s.getId()))
                columnRepo.save(BoardColumn.builder().project(p).status(s).displayName(s.getName()).displayOrder(next++)
                        .enabled(true).build());
        }
    }

    private BoardConfigResponse map(BoardConfig c) {
        return BoardConfigResponse.builder().id(c.getId()).projectId(c.getProject().getId())
                .swimlaneType(c.getSwimlaneType()).enforceWip(c.getEnforceWip())
                .activeSprintOnly(c.getActiveSprintOnly()).showEpic(c.getShowEpic()).build();
    }

    private BoardColumnConfigResponse mapColumn(BoardColumn c, int count) {
        return BoardColumnConfigResponse.builder().id(c.getId()).statusId(c.getStatus().getId())
                .statusName(c.getStatus().getName()).statusColor(c.getStatus().getColor())
                .category(c.getStatus().getCategory().name()).displayName(c.getDisplayName())
                .displayOrder(c.getDisplayOrder()).enabled(c.getEnabled()).wipLimit(c.getWipLimit()).ticketCount(count)
                .build();
    }
}
