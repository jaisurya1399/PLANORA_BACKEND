package com.projectmanagement.app.epic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.milestone.Milestone;
import com.projectmanagement.app.milestone.MilestoneRepository;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.ticket.TicketStatusCategory;

@Service
@Transactional
public class EpicService {

        private final EpicRepository epicRepository;
        private final ProjectRepository projectRepository;
        private final TicketRepository ticketRepository;
        private final MilestoneRepository milestoneRepository;

        public EpicService(
                        EpicRepository epicRepository,
                        ProjectRepository projectRepository,
                        TicketRepository ticketRepository,
                        MilestoneRepository milestoneRepository) {
                this.epicRepository = epicRepository;
                this.projectRepository = projectRepository;
                this.ticketRepository = ticketRepository;
                this.milestoneRepository = milestoneRepository;
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getAllEpics() {

                return epicRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ACTIVE
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getActiveEpics() {

                return epicRepository.findByDeletedAtIsNull()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @Transactional(readOnly = true)
        public EpicResponse getEpicById(Long id) {

                Epic epic = epicRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Epic not found with id: " + id));

                return toResponse(epic);
        }

        // =========================================================
        // GET BY PROJECT
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getEpicsByProject(
                        Long projectId) {

                validateProject(projectId);

                return epicRepository
                                .findByProjectId(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ACTIVE BY PROJECT
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getActiveEpicsByProject(
                        Long projectId) {

                validateProject(projectId);

                return epicRepository
                                .findByProjectIdAndDeletedAtIsNull(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ROOT EPICS
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getRootEpicsByProject(
                        Long projectId) {

                validateProject(projectId);

                return epicRepository
                                .findByProjectIdAndParentIsNull(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET CHILD EPICS
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getChildEpics(
                        Long parentId) {

                validateEpic(parentId);

                return epicRepository
                                .findByParentId(parentId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ACTIVE CHILD EPICS
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getActiveChildEpics(
                        Long parentId) {

                validateEpic(parentId);

                return epicRepository
                                .findByParentIdAndDeletedAtIsNull(parentId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY PROJECT + PARENT
        // =========================================================

        @Transactional(readOnly = true)
        public List<EpicResponse> getEpicsByProjectAndParent(
                        Long projectId,
                        Long parentId) {

                validateProject(projectId);
                validateEpic(parentId);

                return epicRepository
                                .findByProjectIdAndParentId(
                                                projectId,
                                                parentId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY NAME
        // =========================================================

        @Transactional(readOnly = true)
        public EpicResponse getEpicByProjectAndName(
                        Long projectId,
                        String name) {

                validateProject(projectId);

                Epic epic = epicRepository
                                .findByProjectIdAndNameAndDeletedAtIsNull(
                                                projectId,
                                                name)
                                .orElseThrow(() -> new RuntimeException(
                                                "Epic not found with name: " + name));

                return toResponse(epic);
        }

        // =========================================================
        // CREATE
        // =========================================================

        public EpicResponse createEpic(
                        EpicRequest request) {

                validateProject(request.getProjectId());

                validateDateRange(
                                request.getStartsAt(),
                                request.getEndsAt());

                validateEpicName(
                                request.getProjectId(),
                                request.getName(),
                                null);

                Project project = projectRepository
                                .findById(request.getProjectId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found with id: "
                                                                + request.getProjectId()));

                Milestone milestone = getValidMilestone(
                                request.getMilestoneId(),
                                request.getProjectId());

                Epic parent = null;

                if (request.getParentId() != null) {

                        parent = getValidParent(
                                        request.getParentId(),
                                        request.getProjectId(),
                                        null);
                }

                Epic epic = Epic.builder()
                                .project(project)
                                .milestone(milestone)
                                .name(request.getName())
                                .startsAt(request.getStartsAt())
                                .endsAt(request.getEndsAt())
                                .parent(parent)
                                .build();

                return toResponse(
                                epicRepository.save(epic));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        public EpicResponse updateEpic(
                        Long id,
                        EpicRequest request) {

                Epic epic = epicRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Epic not found with id: " + id));

                validateProject(request.getProjectId());

                validateDateRange(
                                request.getStartsAt(),
                                request.getEndsAt());

                validateEpicName(
                                request.getProjectId(),
                                request.getName(),
                                id);

                Project project = projectRepository
                                .findById(request.getProjectId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found with id: "
                                                                + request.getProjectId()));

                Milestone milestone = getValidMilestone(
                                request.getMilestoneId(),
                                request.getProjectId());

                Epic parent = null;

                if (request.getParentId() != null) {

                        parent = getValidParent(
                                        request.getParentId(),
                                        request.getProjectId(),
                                        id);
                }

                epic.setProject(project);
                epic.setMilestone(milestone);
                epic.setName(request.getName());
                epic.setStartsAt(request.getStartsAt());
                epic.setEndsAt(request.getEndsAt());
                epic.setParent(parent);

                return toResponse(
                                epicRepository.save(epic));
        }

        // =========================================================
        // SOFT DELETE
        // =========================================================

        public void deleteEpic(Long id) {

                Epic epic = epicRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Epic not found with id: " + id));

                epic.setDeletedAt(
                                LocalDateTime.now());

                epicRepository.save(epic);
        }

        // =========================================================
        // RESTORE
        // =========================================================

        public EpicResponse restoreEpic(Long id) {

                Epic epic = epicRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Epic not found with id: " + id));

                epic.setDeletedAt(null);

                return toResponse(
                                epicRepository.save(epic));
        }

        // =========================================================
        // PERMANENT DELETE
        // =========================================================

        public void permanentlyDeleteEpic(Long id) {

                if (!epicRepository.existsById(id)) {

                        throw new RuntimeException(
                                        "Epic not found with id: " + id);
                }

                epicRepository.deleteById(id);
        }

        // =========================================================
        // COUNT
        // =========================================================

        @Transactional(readOnly = true)
        public long countEpicsByProject(
                        Long projectId) {

                validateProject(projectId);

                return epicRepository.countByProjectId(projectId);
        }

        @Transactional(readOnly = true)
        public long countChildEpics(
                        Long parentId) {

                validateEpic(parentId);

                return epicRepository.countByParentId(parentId);
        }

        // =========================================================
        // EPIC PROGRESS
        // =========================================================

        @Transactional(readOnly = true)
        public EpicProgressResponse getEpicProgress(Long epicId) {
                Epic epic = getEpicEntity(epicId);
                List<Ticket> tickets = getActiveEpicTickets(epic);

                long total = tickets.size();
                long completed = countByCategory(tickets, TicketStatusCategory.DONE);
                long inProgress = countByCategory(tickets, TicketStatusCategory.IN_PROGRESS);
                long todo = countByCategory(tickets, TicketStatusCategory.TODO);
                long backlog = countByCategory(tickets, TicketStatusCategory.BACKLOG);
                long cancelled = countByCategory(tickets, TicketStatusCategory.CANCELLED);

                BigDecimal totalEstimation = sumEstimation(tickets);
                BigDecimal completedEstimation = sumEstimationByCategory(tickets, TicketStatusCategory.DONE);
                BigDecimal remainingEstimation = totalEstimation.subtract(completedEstimation);

                return EpicProgressResponse.builder()
                                .epicId(epic.getId())
                                .epicName(epic.getName())
                                .projectId(epic.getProject().getId())
                                .totalIssues(total)
                                .completedIssues(completed)
                                .inProgressIssues(inProgress)
                                .todoIssues(todo)
                                .backlogIssues(backlog)
                                .cancelledIssues(cancelled)
                                .totalEstimation(totalEstimation)
                                .completedEstimation(completedEstimation)
                                .remainingEstimation(remainingEstimation)
                                .completionPercentage(percentage(completed, total))
                                .estimationCompletionPercentage(percentage(completedEstimation, totalEstimation))
                                .build();
        }

        // =========================================================
        // EPIC BURNDOWN
        // =========================================================

        @Transactional(readOnly = true)
        public EpicBurndownResponse getEpicBurndown(Long epicId) {
                Epic epic = getEpicEntity(epicId);
                List<Ticket> tickets = getActiveEpicTickets(epic);

                LocalDate start = epic.getStartsAt();
                LocalDate end = epic.getEndsAt();
                LocalDate today = LocalDate.now();
                LocalDate chartEnd = today.isBefore(end) ? today : end;
                if (chartEnd.isBefore(start)) {
                        chartEnd = start;
                }

                BigDecimal total = sumEstimation(tickets);
                long days = Math.max(1, ChronoUnit.DAYS.between(start, end));
                List<EpicBurndownResponse.Point> points = new ArrayList<>();

                for (LocalDate date = start; !date.isAfter(chartEnd); date = date.plusDays(1)) {
                        BigDecimal completedByDate = completedEstimationByDate(tickets, date);
                        BigDecimal remaining = total.subtract(completedByDate).max(BigDecimal.ZERO);
                        long elapsed = Math.max(0, ChronoUnit.DAYS.between(start, date));
                        BigDecimal ideal = total.subtract(
                                        total.multiply(BigDecimal.valueOf(elapsed))
                                                        .divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP))
                                        .max(BigDecimal.ZERO);

                        points.add(EpicBurndownResponse.Point.builder()
                                        .date(date)
                                        .idealRemaining(scale(ideal))
                                        .remaining(scale(remaining))
                                        .completed(scale(completedByDate))
                                        .build());
                }

                return EpicBurndownResponse.builder()
                                .epicId(epic.getId())
                                .epicName(epic.getName())
                                .startsAt(start)
                                .endsAt(end)
                                .totalEstimation(scale(total))
                                .points(points)
                                .build();
        }

        // =========================================================
        // EPIC REPORT
        // =========================================================

        @Transactional(readOnly = true)
        public EpicReportResponse getEpicReport(Long epicId) {
                Epic epic = getEpicEntity(epicId);
                List<Ticket> tickets = getActiveEpicTickets(epic);

                long total = tickets.size();
                long completed = countByCategory(tickets, TicketStatusCategory.DONE);
                long inProgress = countByCategory(tickets, TicketStatusCategory.IN_PROGRESS);
                long todo = countByCategory(tickets, TicketStatusCategory.TODO);
                long backlog = countByCategory(tickets, TicketStatusCategory.BACKLOG);
                long cancelled = countByCategory(tickets, TicketStatusCategory.CANCELLED);

                BigDecimal totalEstimation = sumEstimation(tickets);
                BigDecimal completedEstimation = sumEstimationByCategory(tickets, TicketStatusCategory.DONE);

                Map<String, Long> statuses = new LinkedHashMap<>();
                Map<String, Long> priorities = new LinkedHashMap<>();
                List<EpicReportResponse.IssueSummary> issues = new ArrayList<>();

                for (Ticket ticket : tickets) {
                        String statusName = ticket.getStatus() != null ? ticket.getStatus().getName() : "Unknown";
                        String priorityName = ticket.getPriority() != null ? ticket.getPriority().getName() : "Unknown";
                        statuses.merge(statusName, 1L, Long::sum);
                        priorities.merge(priorityName, 1L, Long::sum);

                        issues.add(EpicReportResponse.IssueSummary.builder()
                                        .id(ticket.getId())
                                        .code(ticket.getCode())
                                        .name(ticket.getName())
                                        .status(statusName)
                                        .statusCategory(ticket.getStatus() != null
                                                        && ticket.getStatus().getCategory() != null
                                                                        ? ticket.getStatus().getCategory().name()
                                                                        : null)
                                        .priority(priorityName)
                                        .estimation(ticket.getEstimation() == null ? BigDecimal.ZERO
                                                        : ticket.getEstimation())
                                        .responsibleId(ticket.getResponsible() != null ? ticket.getResponsible().getId()
                                                        : null)
                                        .responsibleName(ticket.getResponsible() != null
                                                        ? ticket.getResponsible().getName()
                                                        : null)
                                        .createdAt(ticket.getCreatedAt())
                                        .resolvedAt(ticket.getResolvedAt())
                                        .build());
                }

                return EpicReportResponse.builder()
                                .epicId(epic.getId())
                                .epicName(epic.getName())
                                .projectId(epic.getProject().getId())
                                .projectName(epic.getProject().getName())
                                .totalIssues(total)
                                .completedIssues(completed)
                                .inProgressIssues(inProgress)
                                .todoIssues(todo)
                                .backlogIssues(backlog)
                                .cancelledIssues(cancelled)
                                .totalEstimation(scale(totalEstimation))
                                .completedEstimation(scale(completedEstimation))
                                .remainingEstimation(scale(
                                                totalEstimation.subtract(completedEstimation).max(BigDecimal.ZERO)))
                                .completionPercentage(percentage(completed, total))
                                .estimationCompletionPercentage(percentage(completedEstimation, totalEstimation))
                                .statusDistribution(statuses)
                                .priorityDistribution(priorities)
                                .issues(issues)
                                .build();
        }

        private Epic getEpicEntity(Long epicId) {
                return epicRepository.findById(epicId)
                                .orElseThrow(() -> new RuntimeException("Epic not found with id: " + epicId));
        }

        private List<Ticket> getActiveEpicTickets(Epic epic) {
                return ticketRepository.findByProjectIdAndEpicIdAndDeletedAtIsNull(
                                epic.getProject().getId(), epic.getId());
        }

        private long countByCategory(List<Ticket> tickets, TicketStatusCategory category) {
                return tickets.stream()
                                .filter(t -> t.getStatus() != null && category == t.getStatus().getCategory())
                                .count();
        }

        private BigDecimal sumEstimation(List<Ticket> tickets) {
                return tickets.stream()
                                .map(Ticket::getEstimation)
                                .filter(e -> e != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal sumEstimationByCategory(List<Ticket> tickets, TicketStatusCategory category) {
                return tickets.stream()
                                .filter(t -> t.getStatus() != null && category == t.getStatus().getCategory())
                                .map(Ticket::getEstimation)
                                .filter(e -> e != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal completedEstimationByDate(List<Ticket> tickets, LocalDate date) {
                return tickets.stream()
                                .filter(t -> isCompletedOnOrBefore(t, date))
                                .map(Ticket::getEstimation)
                                .filter(e -> e != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private boolean isCompletedOnOrBefore(Ticket ticket, LocalDate date) {
                if (ticket.getStatus() == null || ticket.getStatus().getCategory() != TicketStatusCategory.DONE) {
                        return false;
                }
                if (ticket.getResolvedAt() != null) {
                        return !ticket.getResolvedAt().toLocalDate().isAfter(date);
                }
                // If an old completed ticket has no resolved_at, its exact historical
                // completion
                // date cannot be reconstructed. Treat it as completed on the current day.
                return !LocalDate.now().isAfter(date);
        }

        private double percentage(long value, long total) {
                if (total <= 0)
                        return 0D;
                return round((value * 100D) / total);
        }

        private double percentage(BigDecimal value, BigDecimal total) {
                if (total == null || total.compareTo(BigDecimal.ZERO) <= 0)
                        return 0D;
                return round(value.multiply(BigDecimal.valueOf(100))
                                .divide(total, 2, RoundingMode.HALF_UP).doubleValue());
        }

        private double round(double value) {
                return Math.round(value * 100D) / 100D;
        }

        private BigDecimal scale(BigDecimal value) {
                return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
        }

        // =========================================================
        // VALIDATE PROJECT
        // =========================================================

        private Milestone getValidMilestone(Long milestoneId, Long projectId) {
                Milestone milestone = milestoneRepository.findById(milestoneId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Milestone not found with id: " + milestoneId));

                if (milestone.getProject() == null
                                || !java.util.Objects.equals(milestone.getProject().getId(), projectId)) {
                        throw new IllegalArgumentException(
                                        "Milestone does not belong to the selected project");
                }

                if (milestone.getStatus() == com.projectmanagement.app.milestone.MilestoneStatus.CANCELLED) {
                        throw new IllegalArgumentException(
                                        "Cancelled milestone cannot contain an Epic");
                }

                return milestone;
        }

        private void validateProject(Long projectId) {

                if (!projectRepository.existsById(projectId)) {

                        throw new RuntimeException(
                                        "Project not found with id: " + projectId);
                }
        }

        // =========================================================
        // VALIDATE EPIC
        // =========================================================

        private void validateEpic(Long epicId) {

                if (!epicRepository.existsById(epicId)) {

                        throw new RuntimeException(
                                        "Epic not found with id: " + epicId);
                }
        }

        // =========================================================
        // VALIDATE NAME
        // =========================================================

        private void validateEpicName(
                        Long projectId,
                        String name,
                        Long currentId) {

                boolean exists;

                if (currentId == null) {

                        exists = epicRepository
                                        .existsByProjectIdAndName(
                                                        projectId,
                                                        name);

                } else {

                        exists = epicRepository
                                        .existsByProjectIdAndNameAndIdNot(
                                                        projectId,
                                                        name,
                                                        currentId);
                }

                if (exists) {

                        throw new RuntimeException(
                                        "Epic already exists in this project with name: "
                                                        + name);
                }
        }

        // =========================================================
        // VALIDATE DATE RANGE
        // =========================================================

        private void validateDateRange(
                        java.time.LocalDate startsAt,
                        java.time.LocalDate endsAt) {

                if (startsAt == null || endsAt == null) {
                        return;
                }

                if (endsAt.isBefore(startsAt)) {

                        throw new RuntimeException(
                                        "End date must be on or after start date");
                }
        }

        // =========================================================
        // VALIDATE PARENT
        // =========================================================

        private Epic getValidParent(
                        Long parentId,
                        Long projectId,
                        Long currentEpicId) {

                if (currentEpicId != null &&
                                currentEpicId.equals(parentId)) {

                        throw new RuntimeException(
                                        "An epic cannot be its own parent");
                }

                Epic parent = epicRepository.findById(parentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Parent epic not found with id: "
                                                                + parentId));

                if (!parent.getProject().getId()
                                .equals(projectId)) {

                        throw new RuntimeException(
                                        "Parent epic must belong to the same project");
                }

                if (parent.getDeletedAt() != null) {

                        throw new RuntimeException(
                                        "Cannot assign a deleted epic as parent");
                }

                return parent;
        }

        // =========================================================
        // RESPONSE MAPPER
        // =========================================================

        private EpicResponse toResponse(Epic epic) {

                Project project = epic.getProject();
                Epic parent = epic.getParent();
                Milestone milestone = epic.getMilestone();

                return EpicResponse.builder()

                                .id(epic.getId())

                                .projectId(
                                                project != null
                                                                ? project.getId()
                                                                : null)

                                .projectName(
                                                project != null
                                                                ? project.getName()
                                                                : null)

                                .milestoneId(
                                                milestone != null
                                                                ? milestone.getId()
                                                                : null)

                                .milestoneName(
                                                milestone != null
                                                                ? milestone.getName()
                                                                : null)

                                .name(epic.getName())

                                .startsAt(epic.getStartsAt())
                                .endsAt(epic.getEndsAt())

                                .parentId(
                                                parent != null
                                                                ? parent.getId()
                                                                : null)

                                .parentName(
                                                parent != null
                                                                ? parent.getName()
                                                                : null)

                                .deletedAt(epic.getDeletedAt())
                                .createdAt(epic.getCreatedAt())
                                .updatedAt(epic.getUpdatedAt())

                                .build();
        }
}