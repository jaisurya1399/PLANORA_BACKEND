package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.MemberAvailability;
import com.projectmanagement.app.project.MemberAvailabilityRepository;
import com.projectmanagement.app.project.MemberAvailabilityType;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.project.ProjectUser;
import com.projectmanagement.app.project.ProjectUserRepository;
import com.projectmanagement.app.project.ProjectWorkingHours;
import com.projectmanagement.app.project.ProjectWorkingHoursRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;
import com.projectmanagement.app.ticket.TicketResponse;
import com.projectmanagement.app.ticket.TicketService;
import com.projectmanagement.app.ticket.TicketStatusCategory;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
@Transactional
public class SprintService {

        private final SprintRepository sprintRepository;
        private final ProjectRepository projectRepository;
        private final ProjectUserRepository projectUserRepository;
        private final TicketRepository ticketRepository;
        private final UserRepository userRepository;
        private final ProjectAccessService projectAccessService;
        private final SprintIssueSnapshotRepository snapshotRepository;
        private final SprintCapacityRepository capacityRepository;
        private final MemberAvailabilityRepository availabilityRepository;
        private final ProjectWorkingHoursRepository workingHoursRepository;
        private final TicketService ticketService;

        public SprintService(
                        SprintRepository sprintRepository,
                        ProjectRepository projectRepository,
                        ProjectUserRepository projectUserRepository,
                        TicketRepository ticketRepository,
                        UserRepository userRepository,
                        ProjectAccessService projectAccessService,
                        SprintIssueSnapshotRepository snapshotRepository,
                        SprintCapacityRepository capacityRepository,
                        MemberAvailabilityRepository availabilityRepository,
                        ProjectWorkingHoursRepository workingHoursRepository,
                        TicketService ticketService) {

                this.sprintRepository = sprintRepository;
                this.projectRepository = projectRepository;
                this.projectUserRepository = projectUserRepository;
                this.ticketRepository = ticketRepository;
                this.userRepository = userRepository;
                this.projectAccessService = projectAccessService;
                this.snapshotRepository = snapshotRepository;
                this.capacityRepository = capacityRepository;
                this.availabilityRepository = availabilityRepository;
                this.workingHoursRepository = workingHoursRepository;
                this.ticketService = ticketService;
        }

        // =========================================================
        // CREATE
        // =========================================================

        public SprintResponse create(
                        SprintRequest request,
                        Long userId) {

                Project project = getProject(request.getProjectId());

                validateProjectAccess(project.getId(), userId);

                validateSprintName(
                                project.getId(),
                                request.getName(),
                                null);

                validateDates(
                                request.getStartDate(),
                                request.getEndDate());

                SprintStatus status = request.getStatus() == null
                                ? SprintStatus.PLANNED
                                : request.getStatus();

                if (status == SprintStatus.ACTIVE) {
                        validateNoActiveSprint(project.getId());
                }

                User user = getUser(userId);

                Sprint sprint = Sprint.builder()
                                .name(request.getName().trim())
                                .goal(request.getGoal())
                                .project(project)
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .status(status)
                                .createdBy(user)
                                .build();

                return toResponse(
                                sprintRepository.save(sprint));
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @Transactional(readOnly = true)
        public List<SprintResponse> getAll() {

                return sprintRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @Transactional(readOnly = true)
        public SprintResponse getById(Long id) {

                Sprint sprint = getSprint(id);

                return toResponse(sprint);
        }

        // =========================================================
        // GET PROJECT SPRINTS
        // =========================================================

        @Transactional(readOnly = true)
        public List<SprintResponse> getByProject(Long projectId) {

                if (!projectRepository.existsById(projectId)) {
                        throw new RuntimeException("Project not found");
                }

                return sprintRepository
                                .findByProjectIdOrderByCreatedAtDesc(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // UPDATE
        // =========================================================

        public SprintResponse update(
                        Long id,
                        SprintRequest request,
                        Long userId) {

                Sprint sprint = getSprint(id);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                if (!sprint.getProject().getId().equals(
                                request.getProjectId())) {

                        throw new RuntimeException(
                                        "Sprint project cannot be changed");
                }

                validateSprintName(
                                request.getProjectId(),
                                request.getName(),
                                id);

                validateDates(
                                request.getStartDate(),
                                request.getEndDate());

                SprintStatus newStatus = request.getStatus() == null
                                ? sprint.getStatus()
                                : request.getStatus();

                if (newStatus == SprintStatus.ACTIVE
                                && sprint.getStatus() != SprintStatus.ACTIVE) {

                        validateNoActiveSprint(
                                        sprint.getProject().getId());
                }

                if (sprint.getStatus() == SprintStatus.COMPLETED
                                && newStatus != SprintStatus.COMPLETED) {

                        throw new RuntimeException(
                                        "Completed sprint cannot be reopened");
                }

                sprint.setName(request.getName().trim());
                sprint.setGoal(request.getGoal());
                sprint.setStartDate(request.getStartDate());
                sprint.setEndDate(request.getEndDate());
                sprint.setStatus(newStatus);

                return toResponse(
                                sprintRepository.save(sprint));
        }

        // =========================================================
        // DELETE
        // =========================================================

        public void delete(
                        Long id,
                        Long userId) {

                Sprint sprint = getSprint(id);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                if (sprint.getStatus() == SprintStatus.ACTIVE) {
                        throw new RuntimeException(
                                        "Active sprint cannot be deleted");
                }

                List<Ticket> tickets = ticketRepository
                                .findBySprintIdOrderByOrderAsc(id);

                for (Ticket ticket : tickets) {
                        ticket.setSprint(null);
                }

                ticketRepository.saveAll(tickets);

                sprintRepository.delete(sprint);
        }

        // =========================================================
        // START
        // =========================================================

        public SprintResponse start(
                        Long id,
                        Long userId) {

                Sprint sprint = getSprint(id);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                if (sprint.getStatus() == SprintStatus.COMPLETED) {
                        throw new RuntimeException(
                                        "Completed sprint cannot be started");
                }

                if (sprint.getStatus() == SprintStatus.CANCELLED) {
                        throw new RuntimeException(
                                        "Cancelled sprint cannot be started");
                }

                validateNoActiveSprint(
                                sprint.getProject().getId());

                List<Ticket> sprintTickets = ticketRepository
                                .findBySprintIdOrderByOrderAsc(id)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .toList();

                if (snapshotRepository
                                .findBySprintIdOrderByTicketIdAsc(id)
                                .isEmpty()
                                && !sprintTickets.isEmpty()) {

                        snapshotRepository.saveAll(
                                        sprintTickets.stream()
                                                        .map(ticket -> SprintIssueSnapshot.builder()
                                                                        .sprint(sprint)
                                                                        .ticketId(ticket.getId())
                                                                        .estimation(
                                                                                        nz(ticket.getEstimation()))
                                                                        .resolvedAt(null)
                                                                        .finalStatusCategory(
                                                                                        ticket.getStatus() == null
                                                                                                        ? TicketStatusCategory.TODO
                                                                                                        : ticket.getStatus()
                                                                                                                        .getCategory())
                                                                        .build())
                                                        .toList());
                }

                sprint.setStatus(SprintStatus.ACTIVE);

                return toResponse(
                                sprintRepository.save(sprint));
        }

        // =========================================================
        // COMPLETE
        // =========================================================

        public SprintResponse complete(
                        Long id,
                        Long userId) {

                return complete(
                                id,
                                SprintCompletionRequest.builder()
                                                .moveIncompleteToBacklog(true)
                                                .build(),
                                userId);
        }

        public SprintResponse complete(
                        Long id,
                        SprintCompletionRequest request,
                        Long userId) {

                Sprint sprint = getSprint(id);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                if (sprint.getStatus() != SprintStatus.ACTIVE) {
                        throw new RuntimeException(
                                        "Only active sprint can be completed");
                }

                Sprint carryOverSprint = null;

                if (request.getCarryOverSprintId() != null) {

                        carryOverSprint = sprintRepository
                                        .findByIdAndProjectId(
                                                        request.getCarryOverSprintId(),
                                                        sprint.getProject().getId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Carry-over sprint does not belong to this project"));

                        if (carryOverSprint.getId()
                                        .equals(sprint.getId())) {

                                throw new RuntimeException(
                                                "A sprint cannot carry over into itself");
                        }

                        if (carryOverSprint.getStatus() == SprintStatus.COMPLETED
                                        || carryOverSprint.getStatus() == SprintStatus.CANCELLED) {

                                throw new RuntimeException(
                                                "Cannot carry issues into a completed or cancelled sprint");
                        }
                }

                List<Ticket> sprintTickets = ticketRepository
                                .findBySprintIdOrderByOrderAsc(id)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .toList();

                List<SprintIssueSnapshot> snapshots = snapshotRepository
                                .findBySprintIdOrderByTicketIdAsc(id);

                Map<Long, SprintIssueSnapshot> snapshotByTicket = new LinkedHashMap<>();

                snapshots.forEach(snapshot -> snapshotByTicket.put(
                                snapshot.getTicketId(),
                                snapshot));

                List<SprintIssueSnapshot> finalSnapshots = new ArrayList<>();

                for (Ticket ticket : sprintTickets) {

                        SprintIssueSnapshot snapshot = snapshotByTicket.get(ticket.getId());

                        if (snapshot == null) {

                                snapshot = SprintIssueSnapshot.builder()
                                                .sprint(sprint)
                                                .ticketId(ticket.getId())
                                                .estimation(nz(ticket.getEstimation()))
                                                .build();
                        }

                        if (nz(snapshot.getEstimation()).signum() == 0) {
                                snapshot.setEstimation(
                                                nz(ticket.getEstimation()));
                        }

                        snapshot.setResolvedAt(
                                        ticket.getResolvedAt());

                        snapshot.setFinalStatusCategory(
                                        ticket.getStatus() == null
                                                        ? TicketStatusCategory.TODO
                                                        : ticket.getStatus().getCategory());

                        finalSnapshots.add(snapshot);
                }

                snapshotRepository.saveAll(finalSnapshots);

                List<Ticket> incomplete = sprintTickets.stream()
                                .filter(ticket -> ticket.getStatus() == null
                                                || (ticket.getStatus().getCategory() != TicketStatusCategory.DONE
                                                                && ticket.getStatus()
                                                                                .getCategory() != TicketStatusCategory.CANCELLED))
                                .toList();

                final Sprint finalSprint = carryOverSprint;

                if (finalSprint != null) {

                        incomplete.forEach(ticket -> ticket.setSprint(finalSprint));

                } else if (Boolean.TRUE.equals(
                                request.getMoveIncompleteToBacklog())) {

                        incomplete.forEach(ticket -> ticket.setSprint(null));
                }

                ticketRepository.saveAll(incomplete);

                sprint.setStatus(
                                SprintStatus.COMPLETED);

                return toResponse(
                                sprintRepository.save(sprint));
        }

        // =========================================================
        // BURNDOWN
        // =========================================================

        @Transactional(readOnly = true)
        public SprintBurndownResponse burndown(
                        Long sprintId) {

                Sprint sprint = getSprint(sprintId);

                projectAccessService.requireView(
                                sprint.getProject());

                List<SprintIssueSnapshot> snapshots = snapshotRepository
                                .findBySprintIdOrderByTicketIdAsc(sprintId);

                Map<Long, BigDecimal> committedByTicket = new LinkedHashMap<>();

                snapshots.forEach(snapshot -> committedByTicket.put(
                                snapshot.getTicketId(),
                                nz(snapshot.getEstimation())));

                List<Ticket> liveTickets = ticketRepository
                                .findBySprintIdOrderByOrderAsc(sprintId)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .toList();

                boolean historicalSprint = sprint.getStatus() == SprintStatus.COMPLETED
                                || sprint.getStatus() == SprintStatus.CANCELLED;

                if (!historicalSprint) {

                        committedByTicket.clear();

                        liveTickets.forEach(ticket -> committedByTicket.put(
                                        ticket.getId(),
                                        nz(ticket.getEstimation())));

                } else if (snapshots.isEmpty()) {

                        liveTickets.forEach(ticket -> committedByTicket.put(
                                        ticket.getId(),
                                        nz(ticket.getEstimation())));
                }

                BigDecimal total = committedByTicket.values()
                                .stream()
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                LocalDate start = sprint.getStartDate() == null
                                ? dateOnly(
                                                sprint.getCreatedAt(),
                                                LocalDate.now())
                                : dateOnly(
                                                sprint.getStartDate());

                LocalDate end = sprint.getEndDate() == null
                                ? LocalDate.now()
                                : dateOnly(
                                                sprint.getEndDate());

                if (end.isBefore(start)) {
                        end = start;
                }

                if (!historicalSprint
                                && end.isAfter(LocalDate.now())) {

                        end = LocalDate.now();
                }

                Map<Long, Ticket> liveById = new LinkedHashMap<>();

                liveTickets.forEach(ticket -> liveById.put(
                                ticket.getId(),
                                ticket));

                Map<Long, SprintIssueSnapshot> snapshotByTicket = new LinkedHashMap<>();

                snapshots.forEach(snapshot -> snapshotByTicket.put(
                                snapshot.getTicketId(),
                                snapshot));

                List<SprintProgressPointResponse> points = new ArrayList<>();

                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

                        BigDecimal completed = BigDecimal.ZERO;

                        for (Map.Entry<Long, BigDecimal> entry : committedByTicket.entrySet()) {

                                Ticket ticket = liveById.get(entry.getKey());

                                boolean done = false;

                                if (ticket != null) {

                                        done = ticket.getStatus() != null
                                                        && ticket.getStatus().getCategory() == TicketStatusCategory.DONE
                                                        && ticket.getResolvedAt() != null
                                                        && !dateOnly(
                                                                        ticket.getResolvedAt())
                                                                        .isAfter(date);

                                } else {

                                        SprintIssueSnapshot snapshot = snapshotByTicket.get(
                                                        entry.getKey());

                                        done = snapshot != null
                                                        && snapshot.getFinalStatusCategory() == TicketStatusCategory.DONE
                                                        && snapshot.getResolvedAt() != null
                                                        && !snapshot.getResolvedAt()
                                                                        .toLocalDate()
                                                                        .isAfter(date);
                                }

                                if (done) {
                                        completed = completed.add(entry.getValue());
                                }
                        }

                        BigDecimal remaining = total.subtract(completed)
                                        .max(BigDecimal.ZERO);

                        points.add(
                                        SprintProgressPointResponse.builder()
                                                        .date(date)
                                                        .scopeEstimate(total)
                                                        .completedEstimate(completed)
                                                        .remainingEstimate(remaining)
                                                        .build());
                }

                return SprintBurndownResponse.builder()
                                .sprintId(sprintId)
                                .sprintName(sprint.getName())
                                .totalEstimate(total)
                                .points(points)
                                .build();
        }

        // =========================================================
        // CANCEL
        // =========================================================

        public SprintResponse cancel(
                        Long id,
                        Long userId) {

                Sprint sprint = getSprint(id);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                if (sprint.getStatus() == SprintStatus.COMPLETED) {
                        throw new RuntimeException(
                                        "Completed sprint cannot be cancelled");
                }

                sprint.setStatus(
                                SprintStatus.CANCELLED);

                return toResponse(
                                sprintRepository.save(sprint));
        }

        // =========================================================
        // GET SPRINT TICKETS
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getSprintTickets(
                        Long sprintId) {

                getSprint(sprintId);

                return ticketRepository
                                .findBySprintIdOrderByOrderAsc(sprintId)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .map(ticketService::toResponse)
                                .toList();
        }

        // =========================================================
        // GET PROJECT BACKLOG
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getBacklog(
                        Long projectId) {

                if (!projectRepository.existsById(projectId)) {
                        throw new RuntimeException(
                                        "Project not found");
                }

                return ticketRepository
                                .findByProjectIdAndSprintIsNullOrderByOrderAsc(
                                                projectId)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .map(ticketService::toResponse)
                                .toList();
        }

        // =========================================================
        // ADD TICKET TO SPRINT
        // =========================================================

        public TicketResponse addTicket(
                        Long sprintId,
                        Long ticketId,
                        Long userId) {

                Sprint sprint = getSprint(sprintId);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                Ticket ticket = ticketRepository
                                .findById(ticketId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found"));

                if (ticket.getDeletedAt() != null) {
                        throw new RuntimeException(
                                        "Deleted ticket cannot be added to a sprint");
                }

                if (!ticket.getProject().getId()
                                .equals(sprint.getProject().getId())) {

                        throw new RuntimeException(
                                        "Ticket does not belong to sprint project");
                }

                if (sprint.getStatus() == SprintStatus.COMPLETED
                                || sprint.getStatus() == SprintStatus.CANCELLED) {

                        throw new RuntimeException(
                                        "Ticket cannot be added to completed/cancelled sprint");
                }

                List<Ticket> sprintTickets = ticketRepository
                                .findBySprintIdOrderByOrderAsc(sprintId)
                                .stream()
                                .filter(t -> t.getDeletedAt() == null)
                                .toList();

                ticket.setSprint(sprint);
                ticket.setOrder(sprintTickets.size());

                Ticket savedTicket = ticketRepository.save(ticket);

                if (sprint.getStatus() == SprintStatus.ACTIVE
                                && !snapshotRepository
                                                .existsBySprintIdAndTicketId(
                                                                sprintId,
                                                                ticketId)) {

                        snapshotRepository.save(
                                        SprintIssueSnapshot.builder()
                                                        .sprint(sprint)
                                                        .ticketId(savedTicket.getId())
                                                        .estimation(
                                                                        nz(savedTicket.getEstimation()))
                                                        .resolvedAt(
                                                                        savedTicket.getResolvedAt())
                                                        .finalStatusCategory(
                                                                        savedTicket.getStatus() == null
                                                                                        ? TicketStatusCategory.TODO
                                                                                        : savedTicket.getStatus()
                                                                                                        .getCategory())
                                                        .build());
                }

                return ticketService.toResponse(
                                savedTicket);
        }

        // =========================================================
        // REMOVE TICKET FROM SPRINT
        // =========================================================

        public TicketResponse removeTicket(
                        Long sprintId,
                        Long ticketId,
                        Long userId) {

                Sprint sprint = getSprint(sprintId);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                Ticket ticket = ticketRepository
                                .findById(ticketId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found"));

                if (ticket.getDeletedAt() != null) {
                        throw new RuntimeException(
                                        "Deleted ticket cannot be modified");
                }

                if (ticket.getSprint() == null
                                || !ticket.getSprint().getId()
                                                .equals(sprintId)) {

                        throw new RuntimeException(
                                        "Ticket is not assigned to this sprint");
                }

                ticket.setSprint(null);

                snapshotRepository
                                .findBySprintIdAndTicketId(
                                                sprintId,
                                                ticketId)
                                .ifPresent(
                                                snapshotRepository::delete);

                return ticketService.toResponse(
                                ticketRepository.save(ticket));
        }

        // =========================================================
        // MOVE TICKET TO BACKLOG
        // =========================================================

        public TicketResponse moveToBacklog(
                        Long ticketId,
                        Long userId) {

                Ticket ticket = ticketRepository
                                .findById(ticketId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found"));

                if (ticket.getDeletedAt() != null) {
                        throw new RuntimeException(
                                        "Deleted ticket cannot be modified");
                }

                validateProjectAccess(
                                ticket.getProject().getId(),
                                userId);

                Long previousSprintId = ticket.getSprint() == null
                                ? null
                                : ticket.getSprint().getId();

                ticket.setSprint(null);

                if (previousSprintId != null) {

                        snapshotRepository
                                        .findBySprintIdAndTicketId(
                                                        previousSprintId,
                                                        ticketId)
                                        .ifPresent(
                                                        snapshotRepository::delete);
                }

                return ticketService.toResponse(
                                ticketRepository.save(ticket));
        }

        // =========================================================
        // SPRINT STATISTICS
        // =========================================================

        @Transactional(readOnly = true)
        public SprintStatisticsResponse statistics(
                        Long sprintId) {

                Sprint sprint = getSprint(sprintId);

                projectAccessService.requireView(
                                sprint.getProject());

                List<Ticket> tickets = ticketRepository
                                .findBySprintIdOrderByOrderAsc(sprintId)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .toList();

                long assignedTickets = tickets.stream()
                                .filter(ticket -> ticket.getResponsible() != null
                                                && ticket.getResponsible().getDeletedAt() == null)
                                .count();

                long unassignedTickets = tickets.size() - assignedTickets;

                BigDecimal totalEstimation = tickets.stream()
                                .map(Ticket::getEstimation)
                                .map(this::nz)
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                BigDecimal completedEstimation = tickets.stream()
                                .filter(ticket -> ticket.getStatus() != null
                                                && ticket.getStatus().getCategory() == TicketStatusCategory.DONE)
                                .map(Ticket::getEstimation)
                                .map(this::nz)
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                BigDecimal remainingEstimation = totalEstimation
                                .subtract(completedEstimation)
                                .max(BigDecimal.ZERO);

                BigDecimal completionPercent = totalEstimation.signum() == 0
                                ? BigDecimal.ZERO
                                : completedEstimation
                                                .multiply(BigDecimal.valueOf(100))
                                                .divide(
                                                                totalEstimation,
                                                                2,
                                                                java.math.RoundingMode.HALF_UP);

                return SprintStatisticsResponse.builder()
                                .sprintId(sprint.getId())
                                .sprintName(sprint.getName())
                                .totalTickets((long) tickets.size())
                                .assignedTickets(assignedTickets)
                                .backlogTickets(unassignedTickets)
                                .totalEstimation(totalEstimation)
                                .completedEstimation(completedEstimation)
                                .remainingEstimation(remainingEstimation)
                                .completionPercent(completionPercent)
                                .build();
        }

        // =========================================================
        // SPRINT HISTORY
        // =========================================================

        @Transactional(readOnly = true)
        public List<SprintHistoryResponse> history(
                        Long projectId) {

                Project project = getProject(projectId);

                projectAccessService.requireView(project);

                return sprintRepository
                                .findByProjectIdOrderByCreatedAtDesc(projectId)
                                .stream()
                                .filter(s -> s.getStatus() == SprintStatus.COMPLETED
                                                || s.getStatus() == SprintStatus.CANCELLED)
                                .map(this::historyRow)
                                .toList();
        }

        // =========================================================
        // SPRINT VELOCITY
        // =========================================================

        @Transactional(readOnly = true)
        public SprintVelocityResponse velocity(
                        Long projectId) {

                Project project = getProject(projectId);

                projectAccessService.requireView(project);

                List<SprintVelocityResponse.SprintVelocityPoint> rows = sprintRepository
                                .findByProjectIdAndStatus(
                                                projectId,
                                                SprintStatus.COMPLETED)
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                Sprint::getStartDate,
                                                                Comparator.nullsLast(
                                                                                Comparator.naturalOrder())))
                                .map(s -> {

                                        List<SprintIssueSnapshot> snapshots = snapshotRepository
                                                        .findBySprintIdOrderByTicketIdAsc(
                                                                        s.getId());

                                        BigDecimal committed = snapshots.stream()
                                                        .map(x -> nz(x.getEstimation()))
                                                        .reduce(
                                                                        BigDecimal.ZERO,
                                                                        BigDecimal::add);

                                        BigDecimal completed = snapshots.stream()
                                                        .filter(x -> x.getFinalStatusCategory() == TicketStatusCategory.DONE)
                                                        .map(x -> nz(x.getEstimation()))
                                                        .reduce(
                                                                        BigDecimal.ZERO,
                                                                        BigDecimal::add);

                                        long completedTickets = snapshots.stream()
                                                        .filter(x -> x.getFinalStatusCategory() == TicketStatusCategory.DONE)
                                                        .count();

                                        return SprintVelocityResponse.SprintVelocityPoint
                                                        .builder()
                                                        .sprintId(s.getId())
                                                        .sprintName(s.getName())
                                                        .committedEstimate(committed)
                                                        .completedEstimate(completed)
                                                        .committedTickets(
                                                                        (long) snapshots.size())
                                                        .completedTickets(
                                                                        completedTickets)
                                                        .build();
                                })
                                .toList();

                BigDecimal total = rows.stream()
                                .map(
                                                SprintVelocityResponse.SprintVelocityPoint::getCompletedEstimate)
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                BigDecimal avg = rows.isEmpty()
                                ? BigDecimal.ZERO
                                : total.divide(
                                                BigDecimal.valueOf(rows.size()),
                                                2,
                                                java.math.RoundingMode.HALF_UP);

                return SprintVelocityResponse.builder()
                                .projectId(projectId)
                                .averageVelocity(avg)
                                .totalCompletedEstimate(total)
                                .sprints(rows)
                                .build();
        }

        // =========================================================
        // SPRINT REPORT
        // =========================================================

        @Transactional(readOnly = true)
        public SprintReportResponse report(
                        Long sprintId) {

                Sprint sprint = getSprint(sprintId);

                projectAccessService.requireView(
                                sprint.getProject());

                List<Ticket> current = ticketRepository
                                .findBySprintIdOrderByOrderAsc(sprintId)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .toList();

                List<SprintIssueSnapshot> snapshots = snapshotRepository
                                .findBySprintIdOrderByTicketIdAsc(sprintId);

                boolean historicalSprint = sprint.getStatus() == SprintStatus.COMPLETED
                                || sprint.getStatus() == SprintStatus.CANCELLED;

                if (!historicalSprint) {

                        BigDecimal currentEstimate = current.stream()
                                        .map(ticket -> nz(ticket.getEstimation()))
                                        .reduce(
                                                        BigDecimal.ZERO,
                                                        BigDecimal::add);

                        BigDecimal completed = current.stream()
                                        .filter(ticket -> ticket.getStatus() != null
                                                        && ticket.getStatus()
                                                                        .getCategory() == TicketStatusCategory.DONE)
                                        .map(ticket -> nz(ticket.getEstimation()))
                                        .reduce(
                                                        BigDecimal.ZERO,
                                                        BigDecimal::add);

                        BigDecimal completion = currentEstimate.signum() == 0
                                        ? BigDecimal.ZERO
                                        : completed
                                                        .multiply(BigDecimal.valueOf(100))
                                                        .divide(
                                                                        currentEstimate,
                                                                        2,
                                                                        java.math.RoundingMode.HALF_UP);

                        List<SprintReportResponse.SprintReportIssue> issues = current.stream()
                                        .map(ticket -> SprintReportResponse.SprintReportIssue
                                                        .builder()
                                                        .ticketId(ticket.getId())
                                                        .code(ticket.getCode())
                                                        .name(ticket.getName())
                                                        .estimation(
                                                                        nz(ticket.getEstimation()))
                                                        .status(
                                                                        ticket.getStatus() == null
                                                                                        ? null
                                                                                        : ticket.getStatus()
                                                                                                        .getName())
                                                        .statusCategory(
                                                                        ticket.getStatus() == null
                                                                                        ? null
                                                                                        : ticket.getStatus()
                                                                                                        .getCategory()
                                                                                                        .name())
                                                        .completed(
                                                                        ticket.getStatus() != null
                                                                                        && ticket.getStatus()
                                                                                                        .getCategory() == TicketStatusCategory.DONE)
                                                        .build())
                                        .toList();

                        return SprintReportResponse.builder()
                                        .sprintId(sprintId)
                                        .sprintName(sprint.getName())
                                        .status(sprint.getStatus())
                                        .goal(sprint.getGoal())
                                        .committedTickets(
                                                        (long) current.size())
                                        .completedTickets(
                                                        current.stream()
                                                                        .filter(ticket -> ticket.getStatus() != null
                                                                                        && ticket.getStatus()
                                                                                                        .getCategory() == TicketStatusCategory.DONE)
                                                                        .count())
                                        .incompleteTickets(
                                                        current.stream()
                                                                        .filter(ticket -> ticket.getStatus() == null
                                                                                        || (ticket.getStatus()
                                                                                                        .getCategory() != TicketStatusCategory.DONE
                                                                                                        && ticket.getStatus()
                                                                                                                        .getCategory() != TicketStatusCategory.CANCELLED))
                                                                        .count())
                                        .committedEstimate(currentEstimate)
                                        .completedEstimate(completed)
                                        .remainingEstimate(
                                                        currentEstimate
                                                                        .subtract(completed)
                                                                        .max(BigDecimal.ZERO))
                                        .completionPercent(completion)
                                        .scopeChangeEstimate(BigDecimal.ZERO)
                                        .commitmentCompletionPercent(completion)
                                        .issues(issues)
                                        .build();
                }

                Map<Long, Ticket> currentById = new LinkedHashMap<>();

                current.forEach(ticket -> currentById.put(
                                ticket.getId(),
                                ticket));

                BigDecimal committed = snapshots.stream()
                                .map(snapshot -> nz(snapshot.getEstimation()))
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                BigDecimal completed = snapshots.stream()
                                .filter(snapshot -> snapshot.getFinalStatusCategory() == TicketStatusCategory.DONE)
                                .map(snapshot -> nz(snapshot.getEstimation()))
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                List<SprintReportResponse.SprintReportIssue> issues = snapshots.stream()
                                .map(snapshot -> {

                                        Ticket ticket = currentById.get(
                                                        snapshot.getTicketId());

                                        String status = ticket != null
                                                        && ticket.getStatus() != null
                                                                        ? ticket.getStatus().getName()
                                                                        : snapshot
                                                                                        .getFinalStatusCategory()
                                                                                        .name();

                                        boolean done = snapshot.getFinalStatusCategory() == TicketStatusCategory.DONE;

                                        return SprintReportResponse.SprintReportIssue
                                                        .builder()
                                                        .ticketId(
                                                                        snapshot.getTicketId())
                                                        .code(
                                                                        ticket == null
                                                                                        ? null
                                                                                        : ticket.getCode())
                                                        .name(
                                                                        ticket == null
                                                                                        ? null
                                                                                        : ticket.getName())
                                                        .estimation(
                                                                        nz(snapshot.getEstimation()))
                                                        .status(status)
                                                        .statusCategory(
                                                                        snapshot
                                                                                        .getFinalStatusCategory()
                                                                                        .name())
                                                        .completed(done)
                                                        .build();
                                })
                                .toList();

                BigDecimal completion = committed.signum() == 0
                                ? BigDecimal.ZERO
                                : completed
                                                .multiply(BigDecimal.valueOf(100))
                                                .divide(
                                                                committed,
                                                                2,
                                                                java.math.RoundingMode.HALF_UP);

                BigDecimal currentEstimate = current.stream()
                                .map(ticket -> nz(ticket.getEstimation()))
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                return SprintReportResponse.builder()
                                .sprintId(sprintId)
                                .sprintName(sprint.getName())
                                .status(sprint.getStatus())
                                .goal(sprint.getGoal())
                                .committedTickets(
                                                (long) snapshots.size())
                                .completedTickets(
                                                snapshots.stream()
                                                                .filter(snapshot -> snapshot
                                                                                .getFinalStatusCategory() == TicketStatusCategory.DONE)
                                                                .count())
                                .incompleteTickets(
                                                snapshots.stream()
                                                                .filter(snapshot -> snapshot
                                                                                .getFinalStatusCategory() != TicketStatusCategory.DONE
                                                                                && snapshot
                                                                                                .getFinalStatusCategory() != TicketStatusCategory.CANCELLED)
                                                                .count())
                                .committedEstimate(committed)
                                .completedEstimate(completed)
                                .remainingEstimate(
                                                committed.subtract(completed)
                                                                .max(BigDecimal.ZERO))
                                .completionPercent(completion)
                                .scopeChangeEstimate(
                                                currentEstimate.subtract(committed))
                                .commitmentCompletionPercent(completion)
                                .issues(issues)
                                .build();
        }

        // =========================================================
        // COMMITMENT VS COMPLETION
        // =========================================================

        @Transactional(readOnly = true)
        public SprintCommitmentResponse commitment(
                        Long sprintId) {

                Sprint sprint = getSprint(sprintId);

                projectAccessService.requireView(
                                sprint.getProject());

                List<SprintIssueSnapshot> snapshots = snapshotRepository
                                .findBySprintIdOrderByTicketIdAsc(
                                                sprintId);

                List<Ticket> current = ticketRepository
                                .findBySprintIdOrderByOrderAsc(sprintId)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .toList();

                boolean historicalSprint = sprint.getStatus() == SprintStatus.COMPLETED
                                || sprint.getStatus() == SprintStatus.CANCELLED;

                BigDecimal currentEstimate = current.stream()
                                .map(ticket -> nz(ticket.getEstimation()))
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                BigDecimal committed = snapshots.stream()
                                .map(snapshot -> nz(snapshot.getEstimation()))
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                if (snapshots.isEmpty()
                                && !historicalSprint) {

                        committed = currentEstimate;
                }

                BigDecimal completed = historicalSprint
                                ? snapshots.stream()
                                                .filter(snapshot -> snapshot
                                                                .getFinalStatusCategory() == TicketStatusCategory.DONE)
                                                .map(snapshot -> nz(snapshot.getEstimation()))
                                                .reduce(
                                                                BigDecimal.ZERO,
                                                                BigDecimal::add)
                                : current.stream()
                                                .filter(ticket -> ticket.getStatus() != null
                                                                && ticket.getStatus()
                                                                                .getCategory() == TicketStatusCategory.DONE)
                                                .map(ticket -> nz(ticket.getEstimation()))
                                                .reduce(
                                                                BigDecimal.ZERO,
                                                                BigDecimal::add);

                BigDecimal pct = committed.signum() == 0
                                ? BigDecimal.ZERO
                                : completed
                                                .multiply(BigDecimal.valueOf(100))
                                                .divide(
                                                                committed,
                                                                2,
                                                                java.math.RoundingMode.HALF_UP);

                long committedTickets = historicalSprint
                                ? snapshots.size()
                                : snapshots.isEmpty()
                                                ? current.size()
                                                : snapshots.size();

                long completedTickets = historicalSprint
                                ? snapshots.stream()
                                                .filter(snapshot -> snapshot
                                                                .getFinalStatusCategory() == TicketStatusCategory.DONE)
                                                .count()
                                : current.stream()
                                                .filter(ticket -> ticket.getStatus() != null
                                                                && ticket.getStatus()
                                                                                .getCategory() == TicketStatusCategory.DONE)
                                                .count();

                return SprintCommitmentResponse.builder()
                                .sprintId(sprintId)
                                .sprintName(sprint.getName())
                                .committedEstimate(committed)
                                .completedEstimate(completed)
                                .remainingCommittedEstimate(
                                                committed.subtract(completed)
                                                                .max(BigDecimal.ZERO))
                                .scopeChangeEstimate(
                                                currentEstimate.subtract(committed))
                                .committedTickets(committedTickets)
                                .completedTickets(completedTickets)
                                .currentTickets((long) current.size())
                                .completionPercent(pct)
                                .build();
        }

        // =========================================================
        // CAPACITY PLANNING
        // =========================================================

        @Transactional(readOnly = true)
        public List<SprintCapacityResponse> getCapacity(
                        Long sprintId) {

                Sprint sprint = getSprint(sprintId);

                Project project = sprint.getProject();

                projectAccessService.requireView(project);

                // -----------------------------------------------------
                // Sprint dates
                // -----------------------------------------------------

                LocalDate start = dateOnly(sprint.getStartDate());

                LocalDate end = dateOnly(sprint.getEndDate());

                if (start == null
                                || end == null
                                || end.isBefore(start)) {

                        return List.of();
                }

                // -----------------------------------------------------
                // Configured sprint capacity
                // -----------------------------------------------------
                // IMPORTANT:
                // SprintCapacity belongs to a sprint.
                // Therefore use sprintId here.
                //
                // Deleted users are explicitly excluded.
                // -----------------------------------------------------

                Map<Long, SprintCapacity> configured = new LinkedHashMap<>();

                capacityRepository
                                .findBySprintIdOrderByUser_NameAsc(sprintId)
                                .stream()
                                .filter(capacity -> capacity.getUser() != null)
                                .filter(capacity -> capacity.getUser().getDeletedAt() == null)
                                .forEach(capacity -> configured.put(
                                                capacity.getUser().getId(),
                                                capacity));

                // -----------------------------------------------------
                // Assigned ticket estimation
                // -----------------------------------------------------

                List<Ticket> tickets = ticketRepository
                                .findBySprintIdOrderByOrderAsc(sprintId)
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .toList();

                Map<Long, BigDecimal> assigned = new LinkedHashMap<>();

                tickets.forEach(ticket -> {

                        if (ticket.getResponsible() == null) {
                                return;
                        }

                        // Ignore deleted responsible users.
                        if (ticket.getResponsible().getDeletedAt() != null) {
                                return;
                        }

                        Long userId = ticket.getResponsible().getId();

                        assigned.merge(
                                        userId,
                                        nz(ticket.getEstimation()),
                                        BigDecimal::add);
                });

                // -----------------------------------------------------
                // Member availability
                // -----------------------------------------------------

                List<MemberAvailability> availability = availabilityRepository
                                .findByProjectIdAndAvailabilityDateBetweenOrderByAvailabilityDateAsc(
                                                project.getId(),
                                                start,
                                                end);

                Map<Long, Map<LocalDate, MemberAvailability>> memberAvailability = new LinkedHashMap<>();

                Map<LocalDate, MemberAvailability> projectHolidays = new LinkedHashMap<>();

                for (MemberAvailability entry : availability) {

                        LocalDate availabilityDate = dateOnly(entry.getAvailabilityDate());

                        if (availabilityDate == null) {
                                continue;
                        }

                        // Project-wide holiday.
                        if (entry.getUser() == null) {

                                projectHolidays.put(
                                                availabilityDate,
                                                entry);

                                continue;
                        }

                        // Ignore availability of deleted users.
                        if (entry.getUser().getDeletedAt() != null) {
                                continue;
                        }

                        memberAvailability
                                        .computeIfAbsent(
                                                        entry.getUser().getId(),
                                                        key -> new LinkedHashMap<>())
                                        .put(
                                                        availabilityDate,
                                                        entry);
                }

                // -----------------------------------------------------
                // Working hour history
                // -----------------------------------------------------

                List<ProjectWorkingHours> workingHourHistory = workingHoursRepository
                                .findByProjectIdOrderByEffectiveFromAsc(
                                                project.getId());

                // -----------------------------------------------------
                // ONLY NON-DELETED PROJECT MEMBERS
                // -----------------------------------------------------

                return projectUserRepository
                                .findByProjectIdAndUser_DeletedAtIsNull(
                                                project.getId())
                                .stream()

                                // Defensive null check.
                                .filter(projectUser -> projectUser.getUser() != null)

                                // Defensive soft-delete check.
                                .filter(projectUser -> projectUser.getUser().getDeletedAt() == null)

                                .sorted(
                                                Comparator.comparing(
                                                                (ProjectUser projectUser) -> Objects.toString(
                                                                                projectUser.getUser()
                                                                                                .getName(),
                                                                                ""),
                                                                String.CASE_INSENSITIVE_ORDER))

                                .map(projectUser -> {

                                        Long memberId = projectUser.getUser().getId();

                                        CapacityCalculation calculation = calculateMemberCapacity(
                                                        projectUser,
                                                        start,
                                                        end,
                                                        projectHolidays,
                                                        memberAvailability
                                                                        .getOrDefault(
                                                                                        memberId,
                                                                                        Map.of()),
                                                        workingHourHistory);

                                        BigDecimal hours = nz(
                                                        calculation
                                                                        .getCapacityHours());

                                        SprintCapacity capacity = configured.get(memberId);

                                        BigDecimal points = capacity == null
                                                        ? BigDecimal.ZERO
                                                        : nz(
                                                                        capacity
                                                                                        .getCapacityPoints());

                                        BigDecimal estimate = assigned.getOrDefault(
                                                        memberId,
                                                        BigDecimal.ZERO);

                                        BigDecimal utilization = points.signum() == 0
                                                        ? null
                                                        : estimate
                                                                        .multiply(
                                                                                        BigDecimal.valueOf(100))
                                                                        .divide(
                                                                                        points,
                                                                                        2,
                                                                                        java.math.RoundingMode.HALF_UP);

                                        BigDecimal remainingPoints = points.subtract(estimate);

                                        String capacityStatus;

                                        if (points.signum() <= 0) {

                                                capacityStatus = "NOT_CONFIGURED";

                                        } else if (remainingPoints.signum() < 0) {

                                                capacityStatus = "OVERALLOCATED";

                                        } else if (utilization != null
                                                        && utilization.compareTo(
                                                                        BigDecimal.valueOf(80)) >= 0) {

                                                capacityStatus = "HIGH";

                                        } else {

                                                capacityStatus = "HEALTHY";
                                        }

                                        return SprintCapacityResponse
                                                        .builder()
                                                        .id(
                                                                        capacity == null
                                                                                        ? null
                                                                                        : capacity.getId())
                                                        .sprintId(sprintId)
                                                        .userId(memberId)
                                                        .userName(
                                                                        projectUser
                                                                                        .getUser()
                                                                                        .getName())
                                                        .userEmail(
                                                                        projectUser
                                                                                        .getUser()
                                                                                        .getEmail())
                                                        .capacityPoints(points)
                                                        .capacityHours(hours)
                                                        .baseCapacityHours(
                                                                        calculation
                                                                                        .getBaseCapacityHours())
                                                        .reducedCapacityHours(
                                                                        calculation
                                                                                        .getReducedCapacityHours())
                                                        .workingDays(
                                                                        calculation
                                                                                        .getWorkingDays())
                                                        .holidayDays(
                                                                        calculation
                                                                                        .getHolidayDays())
                                                        .halfDayDays(
                                                                        calculation
                                                                                        .getHalfDayDays())
                                                        .unavailableDays(
                                                                        calculation
                                                                                        .getUnavailableDays())
                                                        .weekendDays(
                                                                        calculation
                                                                                        .getWeekendDays())
                                                        .assignedEstimate(estimate)
                                                        .remainingCapacityPoints(
                                                                        remainingPoints)
                                                        .utilizationPercent(
                                                                        utilization)
                                                        .capacityStatus(
                                                                        capacityStatus)
                                                        .build();
                                })
                                .toList();
        }

        // =========================================================
        // SAVE CAPACITY
        // =========================================================

        /**
         * Saves only manually planned capacity points.
         *
         * Capacity hours are always calculated from project working
         * hours and member availability.
         */
        public SprintCapacityResponse saveCapacity(
                        Long sprintId,
                        SprintCapacityRequest request,
                        Long userId) {

                Sprint sprint = getSprint(sprintId);

                validateProjectAccess(
                                sprint.getProject().getId(),
                                userId);

                if (request == null
                                || request.getUserId() == null) {

                        throw new IllegalArgumentException(
                                        "User is required");
                }

                // -----------------------------------------------------
                // IMPORTANT:
                // User must be an ACTIVE project member.
                // -----------------------------------------------------

                boolean activeMember = projectUserRepository
                                .findByProjectIdAndUser_DeletedAtIsNull(
                                                sprint.getProject().getId())
                                .stream()
                                .filter(projectUser -> projectUser.getUser() != null)
                                .anyMatch(projectUser -> projectUser.getUser().getId()
                                                .equals(request.getUserId()));

                if (!activeMember) {

                        throw new RuntimeException(
                                        "User is not an active member of this project");
                }

                SprintCapacity capacity = capacityRepository
                                .findBySprintIdAndUserId(
                                                sprintId,
                                                request.getUserId())
                                .orElseGet(
                                                SprintCapacity::new);

                User user = getUser(request.getUserId());

                // Do not allow a deleted user to receive capacity.
                if (user.getDeletedAt() != null) {

                        throw new RuntimeException(
                                        "Deleted user cannot have sprint capacity");
                }

                capacity.setSprint(sprint);
                capacity.setUser(user);

                capacity.setCapacityPoints(
                                nz(
                                                request.getCapacityPoints()).max(BigDecimal.ZERO));

                // Never persist client supplied capacity hours.
                capacity.setCapacityHours(
                                BigDecimal.ZERO);

                capacityRepository.save(capacity);

                return getCapacity(sprintId)
                                .stream()
                                .filter(response -> response.getUserId()
                                                .equals(request.getUserId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException(
                                                "Capacity could not be saved"));
        }

        // =========================================================
        // CAPACITY CALCULATION
        // =========================================================

        private CapacityCalculation calculateMemberCapacity(
                        ProjectUser projectUser,
                        LocalDate start,
                        LocalDate end,
                        Map<LocalDate, MemberAvailability> projectHolidays,
                        Map<LocalDate, MemberAvailability> memberEntries,
                        List<ProjectWorkingHours> workingHourHistory) {

                LocalDate joinDate = dateOnly(
                                projectUser.getCreatedAt(),
                                start);

                BigDecimal baseCapacityHours = BigDecimal.ZERO;

                BigDecimal capacityHours = BigDecimal.ZERO;

                int holidayDays = 0;
                int halfDayDays = 0;
                int unavailableDays = 0;
                int weekendDays = 0;
                int workingDays = 0;

                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

                        if (date.isBefore(joinDate)) {
                                continue;
                        }

                        // Saturday / Sunday.
                        if (date.getDayOfWeek().getValue() >= 6) {

                                weekendDays++;

                                continue;
                        }

                        BigDecimal dayHours = effectiveWorkingHours(
                                        workingHourHistory,
                                        date);

                        baseCapacityHours = baseCapacityHours.add(dayHours);

                        // -------------------------------------------------
                        // Project-wide holiday.
                        // -------------------------------------------------

                        MemberAvailability projectEntry = projectHolidays.get(date);

                        if (isLeave(projectEntry)) {

                                holidayDays++;

                                continue;
                        }

                        // -------------------------------------------------
                        // Member-specific availability.
                        // -------------------------------------------------

                        MemberAvailability entry = memberEntries.get(date);

                        if (isLeave(entry)) {

                                if (entry.getAvailabilityType() == MemberAvailabilityType.HOLIDAY) {

                                        holidayDays++;

                                } else {

                                        unavailableDays++;
                                }

                                continue;
                        }

                        workingDays++;

                        // -------------------------------------------------
                        // Half day.
                        // -------------------------------------------------

                        if (entry != null
                                        && entry.getAvailabilityType() == MemberAvailabilityType.HALF_DAY) {

                                halfDayDays++;

                                capacityHours = capacityHours.add(
                                                dayHours.divide(
                                                                BigDecimal.valueOf(2),
                                                                2,
                                                                java.math.RoundingMode.HALF_UP));

                        } else if (entry != null
                                        && entry.getAvailabilityType() == MemberAvailabilityType.AVAILABLE
                                        && entry.getAvailableHours() != null) {

                                capacityHours = capacityHours.add(
                                                entry.getAvailableHours()
                                                                .max(BigDecimal.ZERO)
                                                                .min(dayHours));

                        } else {

                                capacityHours = capacityHours.add(dayHours);
                        }
                }

                BigDecimal reducedCapacityHours = baseCapacityHours
                                .subtract(capacityHours)
                                .max(BigDecimal.ZERO)
                                .setScale(
                                                2,
                                                java.math.RoundingMode.HALF_UP);

                return new CapacityCalculation(
                                capacityHours.setScale(
                                                2,
                                                java.math.RoundingMode.HALF_UP),
                                baseCapacityHours.setScale(
                                                2,
                                                java.math.RoundingMode.HALF_UP),
                                reducedCapacityHours,
                                workingDays,
                                holidayDays,
                                halfDayDays,
                                unavailableDays,
                                weekendDays);
        }

        // =========================================================
        // AVAILABILITY
        // =========================================================

        private boolean isLeave(
                        MemberAvailability entry) {

                if (entry == null
                                || entry.getAvailabilityType() == null) {

                        return false;
                }

                return entry.getAvailabilityType() == MemberAvailabilityType.HOLIDAY
                                || entry.getAvailabilityType() == MemberAvailabilityType.UNAVAILABLE;
        }

        // =========================================================
        // CAPACITY CALCULATION DTO
        // =========================================================

        private static class CapacityCalculation {

                private final BigDecimal capacityHours;
                private final BigDecimal baseCapacityHours;
                private final BigDecimal reducedCapacityHours;

                private final int workingDays;
                private final int holidayDays;
                private final int halfDayDays;
                private final int unavailableDays;
                private final int weekendDays;

                CapacityCalculation(
                                BigDecimal capacityHours,
                                BigDecimal baseCapacityHours,
                                BigDecimal reducedCapacityHours,
                                int workingDays,
                                int holidayDays,
                                int halfDayDays,
                                int unavailableDays,
                                int weekendDays) {

                        this.capacityHours = capacityHours;
                        this.baseCapacityHours = baseCapacityHours;
                        this.reducedCapacityHours = reducedCapacityHours;
                        this.workingDays = workingDays;
                        this.holidayDays = holidayDays;
                        this.halfDayDays = halfDayDays;
                        this.unavailableDays = unavailableDays;
                        this.weekendDays = weekendDays;
                }

                public BigDecimal getCapacityHours() {
                        return capacityHours;
                }

                public BigDecimal getBaseCapacityHours() {
                        return baseCapacityHours;
                }

                public BigDecimal getReducedCapacityHours() {
                        return reducedCapacityHours;
                }

                public int getWorkingDays() {
                        return workingDays;
                }

                public int getHolidayDays() {
                        return holidayDays;
                }

                public int getHalfDayDays() {
                        return halfDayDays;
                }

                public int getUnavailableDays() {
                        return unavailableDays;
                }

                public int getWeekendDays() {
                        return weekendDays;
                }
        }

        // =========================================================
        // DATE HELPERS
        // =========================================================

        private LocalDate dateOnly(
                        LocalDateTime value,
                        LocalDate fallback) {

                return value == null
                                ? fallback
                                : value.toLocalDate();
        }

        private LocalDate dateOnly(
                        LocalDateTime value) {

                return value == null
                                ? null
                                : value.toLocalDate();
        }

        private LocalDate dateOnly(
                        LocalDate value) {

                return value;
        }

        // =========================================================
        // WORKING HOURS
        // =========================================================

        private BigDecimal effectiveWorkingHours(
                        List<ProjectWorkingHours> history,
                        LocalDate date) {

                for (int i = history.size() - 1; i >= 0; i--) {

                        ProjectWorkingHours record = history.get(i);

                        if (record.getEffectiveFrom() != null
                                        && !record.getEffectiveFrom()
                                                        .isAfter(date)) {

                                return nz(
                                                record.getWorkingHours()).max(BigDecimal.ZERO);
                        }
                }

                // Default working day = 8 hours.
                return BigDecimal.valueOf(8);
        }

        // =========================================================
        // HISTORY
        // =========================================================

        private SprintHistoryResponse historyRow(
                        Sprint sprint) {

                List<SprintIssueSnapshot> snapshots = snapshotRepository
                                .findBySprintIdOrderByTicketIdAsc(
                                                sprint.getId());

                BigDecimal committed = snapshots.stream()
                                .map(snapshot -> nz(snapshot.getEstimation()))
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                BigDecimal completed = snapshots.stream()
                                .filter(snapshot -> snapshot.getFinalStatusCategory() == TicketStatusCategory.DONE)
                                .map(snapshot -> nz(snapshot.getEstimation()))
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                BigDecimal pct = committed.signum() == 0
                                ? BigDecimal.ZERO
                                : completed
                                                .multiply(BigDecimal.valueOf(100))
                                                .divide(
                                                                committed,
                                                                2,
                                                                java.math.RoundingMode.HALF_UP);

                return SprintHistoryResponse.builder()
                                .sprintId(sprint.getId())
                                .sprintName(sprint.getName())
                                .status(sprint.getStatus())
                                .startDate(sprint.getStartDate())
                                .endDate(sprint.getEndDate())
                                .committedTickets(
                                                (long) snapshots.size())
                                .completedTickets(
                                                snapshots.stream()
                                                                .filter(snapshot -> snapshot
                                                                                .getFinalStatusCategory() == TicketStatusCategory.DONE)
                                                                .count())
                                .committedEstimate(committed)
                                .completedEstimate(completed)
                                .completionPercent(pct)
                                .build();
        }

        // =========================================================
        // NULL-SAFE BIG DECIMAL
        // =========================================================

        private BigDecimal nz(
                        BigDecimal value) {

                return value == null
                                ? BigDecimal.ZERO
                                : value;
        }

        // =========================================================
        // HELPERS
        // =========================================================

        private Sprint getSprint(Long id) {

                return sprintRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Sprint not found"));
        }

        private Project getProject(Long id) {

                return projectRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found"));
        }

        private User getUser(Long id) {

                return userRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found"));
        }

        private void validateProjectAccess(
                        Long projectId,
                        Long userId) {

                if (userId == null) {

                        throw new RuntimeException(
                                        "Authenticated user not found");
                }

                projectAccessService.requireEditor(
                                getProject(projectId));
        }

        private void validateNoActiveSprint(
                        Long projectId) {

                boolean active = sprintRepository
                                .existsByProjectIdAndStatus(
                                                projectId,
                                                SprintStatus.ACTIVE);

                if (active) {

                        throw new RuntimeException(
                                        "Another active sprint already exists for this project");
                }
        }

        private void validateSprintName(
                        Long projectId,
                        String name,
                        Long currentSprintId) {

                if (name == null
                                || name.trim().isEmpty()) {

                        throw new RuntimeException(
                                        "Sprint name is required");
                }

                boolean exists;

                if (currentSprintId == null) {

                        exists = sprintRepository
                                        .existsByProjectIdAndName(
                                                        projectId,
                                                        name.trim());

                } else {

                        exists = sprintRepository
                                        .existsByProjectIdAndNameAndIdNot(
                                                        projectId,
                                                        name.trim(),
                                                        currentSprintId);
                }

                if (exists) {

                        throw new RuntimeException(
                                        "Sprint with this name already exists in this project");
                }
        }

        private void validateDates(
                        LocalDate startDate,
                        LocalDate endDate) {

                if (startDate != null
                                && endDate != null
                                && endDate.isBefore(startDate)) {

                        throw new RuntimeException(
                                        "End date cannot be before start date");
                }
        }

        // =========================================================
        // RESPONSE
        // =========================================================

        private SprintResponse toResponse(
                        Sprint sprint) {

                Long ticketCount = ticketRepository
                                .countBySprintId(
                                                sprint.getId());

                BigDecimal totalEstimation = ticketRepository
                                .findBySprintIdOrderByOrderAsc(
                                                sprint.getId())
                                .stream()
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .map(Ticket::getEstimation)
                                .filter(Objects::nonNull)
                                .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add);

                return SprintResponse.builder()
                                .id(sprint.getId())
                                .name(sprint.getName())
                                .goal(sprint.getGoal())
                                .projectId(
                                                sprint.getProject().getId())
                                .projectName(
                                                sprint.getProject().getName())
                                .startDate(
                                                sprint.getStartDate())
                                .endDate(
                                                sprint.getEndDate())
                                .status(
                                                sprint.getStatus())
                                .createdBy(
                                                sprint.getCreatedBy() == null
                                                                ? null
                                                                : sprint.getCreatedBy().getId())
                                .createdAt(
                                                sprint.getCreatedAt())
                                .updatedAt(
                                                sprint.getUpdatedAt())
                                .ticketCount(ticketCount)
                                .totalEstimation(totalEstimation)
                                .build();
        }
}