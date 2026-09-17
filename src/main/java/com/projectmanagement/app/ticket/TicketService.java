package com.projectmanagement.app.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.audit.AuditService;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.board.BoardService;
import com.projectmanagement.app.customfield.CustomFieldValueService;
import com.projectmanagement.app.epic.Epic;
import com.projectmanagement.app.epic.EpicRepository;
import com.projectmanagement.app.label.Label;
import com.projectmanagement.app.label.LabelRepository;
import com.projectmanagement.app.milestone.Milestone;
import com.projectmanagement.app.milestone.MilestoneRepository;
import com.projectmanagement.app.notification.TicketNotificationService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.project.ProjectUserRepository;
import com.projectmanagement.app.realtime.RealtimeEventService;
import com.projectmanagement.app.securityscheme.IssueSecurityLevel;
import com.projectmanagement.app.securityscheme.IssueSecuritySchemeRepository;
import com.projectmanagement.app.sprint.Sprint;
import com.projectmanagement.app.sprint.SprintIssueSnapshot;
import com.projectmanagement.app.sprint.SprintIssueSnapshotRepository;
import com.projectmanagement.app.sprint.SprintRepository;
import com.projectmanagement.app.sprint.SprintStatus;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;
import com.projectmanagement.app.workflow.WorkflowService;

@Service
@Transactional
public class TicketService {

        private final TicketRepository ticketRepository;
        private final ProjectRepository projectRepository;
        private final ProjectUserRepository projectUserRepository;
        private final UserRepository userRepository;
        private final TicketStatusRepository ticketStatusRepository;
        private final TicketTypeRepository ticketTypeRepository;
        private final TicketPriorityRepository ticketPriorityRepository;
        private final EpicRepository epicRepository;
        private final SprintRepository sprintRepository;
        private final MilestoneRepository milestoneRepository;
        private final LabelRepository labelRepository;
        private final ProjectAccessService projectAccessService;
        private final TicketActivityRepository ticketActivityRepository;
        private final CurrentUserService currentUserService;
        private final TicketNotificationService ticketNotificationService;
        private final AuditService auditService;
        private final RealtimeEventService realtimeEvents;
        private final BoardService boardService;
        private final CustomFieldValueService customFieldValueService;
        private final WorkflowService workflowService;
        private final IssueSecuritySchemeRepository issueSecuritySchemes;
        private final SprintIssueSnapshotRepository snapshotRepository;

        public TicketService(
                        TicketRepository ticketRepository,
                        ProjectRepository projectRepository,
                        ProjectUserRepository projectUserRepository,
                        UserRepository userRepository,
                        TicketStatusRepository ticketStatusRepository,
                        TicketTypeRepository ticketTypeRepository,
                        TicketPriorityRepository ticketPriorityRepository,
                        EpicRepository epicRepository,
                        SprintRepository sprintRepository,
                        MilestoneRepository milestoneRepository,
                        LabelRepository labelRepository,
                        ProjectAccessService projectAccessService,
                        TicketActivityRepository ticketActivityRepository,
                        CurrentUserService currentUserService,
                        TicketNotificationService ticketNotificationService,
                        AuditService auditService,
                        RealtimeEventService realtimeEvents,
                        BoardService boardService,
                        CustomFieldValueService customFieldValueService,
                        WorkflowService workflowService,
                        IssueSecuritySchemeRepository issueSecuritySchemes,
                        SprintIssueSnapshotRepository snapshotRepository) {

                this.ticketRepository = ticketRepository;
                this.projectRepository = projectRepository;
                this.projectUserRepository = projectUserRepository;
                this.userRepository = userRepository;
                this.ticketStatusRepository = ticketStatusRepository;
                this.ticketTypeRepository = ticketTypeRepository;
                this.ticketPriorityRepository = ticketPriorityRepository;
                this.epicRepository = epicRepository;
                this.sprintRepository = sprintRepository;
                this.milestoneRepository = milestoneRepository;
                this.labelRepository = labelRepository;
                this.projectAccessService = projectAccessService;
                this.ticketActivityRepository = ticketActivityRepository;
                this.currentUserService = currentUserService;
                this.ticketNotificationService = ticketNotificationService;
                this.auditService = auditService;
                this.realtimeEvents = realtimeEvents;
                this.boardService = boardService;
                this.customFieldValueService = customFieldValueService;
                this.workflowService = workflowService;
                this.issueSecuritySchemes = issueSecuritySchemes;
                this.snapshotRepository = snapshotRepository;
        }

        // ============================================================
        // GET ALL
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getAll() {

                return ticketRepository.findAll()
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET ACTIVE
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getAllActive() {

                return ticketRepository
                                .findActiveTickets()
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET DELETED
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getDeleted() {

                return ticketRepository
                                .findByDeletedAtIsNotNull()
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET RESOLVED / HISTORY
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getResolved() {
                return ticketRepository.findResolvedTickets()
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TicketResponse> getResolvedByProject(Long projectId) {
                projectAccessService.requireView(getProject(projectId));
                return ticketRepository.findResolvedByProjectId(projectId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET MY TASKS
        // ============================================================
        // Uses currently authenticated user's email from JWT.
        //
        // Then finds the corresponding User record.
        //
        // Finally gets tickets where:
        //
        // tickets.responsible_id = currentUser.id
        //
        // Deleted tickets are excluded.
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getMyTasks() {

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (authentication == null ||
                                !authentication.isAuthenticated() ||
                                authentication.getName() == null ||
                                authentication.getName().isBlank()) {

                        throw new RuntimeException(
                                        "User is not authenticated");
                }

                String email = authentication
                                .getName()
                                .trim();

                User currentUser = userRepository
                                .findByEmail(email)
                                .filter(user -> user.getDeletedAt() == null)
                                .orElseThrow(() -> new RuntimeException(
                                                "Current user not found with email: "
                                                                + email));

                return ticketRepository
                                .findActiveByResponsibleId(currentUser.getId())
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TicketResponse> getMyTasksAll() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null
                                || authentication.getName().isBlank())
                        throw new RuntimeException("User is not authenticated");
                User currentUser = userRepository.findByEmail(authentication.getName().trim())
                                .filter(user -> user.getDeletedAt() == null)
                                .orElseThrow(() -> new RuntimeException(
                                                "Current user not found with email: " + authentication.getName()));
                return ticketRepository.findByResponsibleIdAndDeletedAtIsNull(currentUser.getId()).stream()
                                .filter(this::canView).map(this::toResponse).toList();
        }

        @Transactional(readOnly = true)
        public List<TicketResponse> getMyResolvedTasks() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null
                                || authentication.getName().isBlank())
                        throw new RuntimeException("User is not authenticated");
                User currentUser = userRepository.findByEmail(authentication.getName().trim())
                                .filter(user -> user.getDeletedAt() == null)
                                .orElseThrow(() -> new RuntimeException(
                                                "Current user not found with email: " + authentication.getName()));
                return ticketRepository.findResolvedByResponsibleId(currentUser.getId()).stream()
                                .filter(this::canView).map(this::toResponse).toList();
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @Transactional(readOnly = true)
        public TicketResponse getById(Long id) {

                Ticket ticket = ticketRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found with id: " + id));
                projectAccessService.requireView(ticket.getProject());

                return toResponse(ticket);
        }

        // ============================================================
        // GET BY CODE
        // ============================================================

        @Transactional(readOnly = true)
        public TicketResponse getByCode(String code) {

                Ticket ticket = ticketRepository
                                .findByCode(code)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found with code: " + code));
                projectAccessService.requireView(ticket.getProject());

                return toResponse(ticket);
        }

        // ============================================================
        // GET BY PROJECT
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getByProject(
                        Long projectId) {

                projectAccessService.requireView(getProject(projectId));

                return ticketRepository
                                .findByProjectIdOrderByOrderAsc(projectId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET ACTIVE BY PROJECT
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getActiveByProject(
                        Long projectId) {

                projectAccessService.requireView(getProject(projectId));

                return ticketRepository
                                .findActiveByProjectId(projectId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TicketResponse> getRootTicketsByProject(Long projectId) {
                projectAccessService.requireView(getProject(projectId));
                return ticketRepository.findByProjectIdAndParentIsNullAndDeletedAtIsNullOrderByOrderAscIdAsc(projectId)
                                .stream().filter(this::canView).map(this::toResponse).toList();
        }

        @Transactional(readOnly = true)
        public List<TicketResponse> getChildren(Long parentId) {
                Ticket parent = ticketRepository.findById(parentId)
                                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + parentId));
                projectAccessService.requireView(parent.getProject());
                return ticketRepository.findByParentIdAndDeletedAtIsNullOrderByOrderAscIdAsc(parentId)
                                .stream().filter(this::canView).map(this::toResponse).toList();
        }

        @Transactional(readOnly = true)
        public List<BoardColumnResponse> getBoard(Long projectId) {
                Project project = getProject(projectId);
                projectAccessService.requireView(project);
                List<TicketStatus> statuses = new ArrayList<>(
                                ticketStatusRepository.findByProjectIdAndDeletedAtIsNullOrderByOrderAsc(projectId));
                statuses.addAll(ticketStatusRepository.findByProjectIdIsNullAndDeletedAtIsNull());
                return statuses.stream()
                                .sorted(Comparator.comparing(TicketStatus::getOrder).thenComparing(TicketStatus::getId))
                                .map(status -> BoardColumnResponse.builder()
                                                .statusId(status.getId()).statusName(status.getName())
                                                .statusColor(status.getColor())
                                                .category(status.getCategory()).order(status.getOrder())
                                                .tickets(ticketRepository
                                                                .findByProjectIdAndStatusIdAndDeletedAtIsNullOrderByOrderAsc(
                                                                                projectId, status.getId())
                                                                .stream().map(this::toResponse).toList())
                                                .build())
                                .toList();
        }

        // ============================================================
        // GET BY OWNER
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getByOwner(
                        Long ownerId) {

                return ticketRepository
                                .findByOwnerId(ownerId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY RESPONSIBLE
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getByResponsible(
                        Long responsibleId) {

                return ticketRepository
                                .findByResponsibleId(responsibleId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY STATUS
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getByStatus(
                        Long statusId) {

                return ticketRepository
                                .findByStatusId(statusId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY TYPE
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getByType(
                        Long typeId) {

                return ticketRepository
                                .findByTypeId(typeId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY PRIORITY
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getByPriority(
                        Long priorityId) {

                return ticketRepository
                                .findByPriorityId(priorityId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET BY EPIC
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> getByEpic(
                        Long epicId) {

                return ticketRepository
                                .findByEpicId(epicId)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // SEARCH
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> search(
                        String name) {

                return ticketRepository
                                .findByNameContainingIgnoreCase(name)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // SEARCH IN PROJECT
        // ============================================================

        @Transactional(readOnly = true)
        public List<TicketResponse> searchInProject(
                        Long projectId,
                        String name) {

                projectAccessService.requireView(getProject(projectId));

                return ticketRepository
                                .findByProjectIdAndNameContainingIgnoreCase(
                                                projectId,
                                                name)
                                .stream()
                                .filter(this::canView)
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TicketPageResponse filter(Long projectId, TicketFilterRequest filter) {
                projectAccessService.requireView(getProject(projectId));
                String requestedSort = filter.getSort() == null || filter.getSort().isBlank() ? "order"
                                : filter.getSort();
                String sortProperty = switch (requestedSort) {
                        case "createdAt", "updatedAt", "code", "name", "order", "estimation" -> requestedSort;
                        default -> throw new RuntimeException("Unsupported sort field");
                };
                Sort.Direction direction = "DESC".equalsIgnoreCase(filter.getDirection()) ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                Page<Ticket> page = ticketRepository.searchActiveByProject(projectId,
                                blankToNull(filter.getQ()), filter.getStatusId(), filter.getPriorityId(),
                                filter.getResponsibleId(),
                                filter.getSprintId(), filter.getEpicId(), filter.getLabelId(),
                                Boolean.TRUE.equals(filter.getRootOnly()),
                                PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, sortProperty)));
                return TicketPageResponse.builder().items(page.getContent().stream().map(this::toResponse).toList())
                                .page(page.getNumber()).size(page.getSize()).totalItems(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .first(page.isFirst()).last(page.isLast()).build();
        }

        private TicketPageResponse toPageResponse(Page<Ticket> page) {
                return TicketPageResponse.builder()
                                .items(page.getContent().stream().map(this::toResponse).toList())
                                .page(page.getNumber())
                                .size(page.getSize())
                                .totalItems(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .first(page.isFirst())
                                .last(page.isLast())
                                .build();
        }

        // ============================================================
        // CREATE
        // ============================================================

        public TicketResponse create(
                        TicketRequest request) {

                Project project = projectRepository
                                .findById(request.getProjectId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found with id: "
                                                                + request.getProjectId()));
                projectAccessService.requireEditor(project);
                if (request.getSecurityLevel() != null && !projectAccessService.canManage(project))
                        throw new RuntimeException("Only project admins can set issue security");
                validateProjectActive(project);

                User owner = userRepository
                                .findById(request.getOwnerId())
                                .filter(user -> user.getDeletedAt() == null)
                                .orElseThrow(() -> new RuntimeException(
                                                "Owner not found with id: "
                                                                + request.getOwnerId()));

                User responsible = null;

                if (request.getResponsibleId() != null) {

                        responsible = userRepository
                                        .findById(request.getResponsibleId())
                                        .filter(user -> user.getDeletedAt() == null)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Responsible user not found with id: "
                                                                        + request.getResponsibleId()));
                }

                validateTicketParticipantBelongsToProject(owner, project, "Owner");
                validateTicketParticipantBelongsToProject(responsible, project, "Responsible user");

                TicketStatus status = ticketStatusRepository
                                .findById(request.getStatusId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with id: "
                                                                + request.getStatusId()));
                validateStatusBelongsToProject(status, project);

                TicketType type = ticketTypeRepository
                                .findById(request.getTypeId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket type not found with id: "
                                                                + request.getTypeId()));
                validateTypeActive(type);

                TicketPriority priority = ticketPriorityRepository
                                .findById(request.getPriorityId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket priority not found with id: "
                                                                + request.getPriorityId()));
                validatePriorityActive(priority);

                Epic epic = null;

                if (request.getEpicId() != null) {

                        epic = epicRepository
                                        .findById(request.getEpicId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Epic not found with id: "
                                                                        + request.getEpicId()));

                        validateEpicBelongsToProject(
                                        epic,
                                        project);
                }

                Ticket parent = resolveParent(request.getParentId(), project, null);

                Sprint sprint = resolveSprint(request.getSprintId(), project);
                Milestone milestone = resolveMilestone(request.getMilestoneId(), project);
                Set<Label> labels = resolveLabels(request.getLabelIds(), project);

                // Ticket code is always generated by the backend.
                // Clients are not allowed to provide or override it.
                String code = generateTicketCode(project);

                Ticket ticket = Ticket.builder()
                                .name(request.getName().trim())
                                .content(request.getContent())
                                .owner(owner)
                                .responsible(responsible)
                                .dueDate(request.getDueDate())
                                .status(status)
                                .resolvedAt(isTerminalStatus(status) ? LocalDateTime.now() : null)
                                .project(project)
                                .code(code)
                                .type(type)
                                .order(
                                                request.getOrder() != null
                                                                ? request.getOrder()
                                                                : 0)
                                .priority(priority)
                                .estimation(
                                                request.getEstimation() != null
                                                                ? request.getEstimation()
                                                                : BigDecimal.ZERO)
                                .epic(epic)
                                .parent(parent)
                                .sprint(sprint)
                                .milestone(milestone)
                                .labels(labels)
                                .securityLevel(request.getSecurityLevel() != null ? request.getSecurityLevel()
                                                : issueSecuritySchemes.findByProjectId(project.getId())
                                                                .map(x -> x.getDefaultLevel())
                                                                .orElse(IssueSecurityLevel.PROJECT))
                                .build();

                Ticket saved = ticketRepository.save(ticket);
                syncSprintCommitment(null, saved);
                customFieldValueService.replaceValues(saved, request.getCustomFields(), project.getId(), type.getId());
                ticketNotificationService.notifyAssignment(saved, currentUserService.getCurrentUser());
                auditService.record(project, saved, "TICKET_CREATED", "TICKET", saved.getId(), ticketSnapshot(saved));
                publishTicketEvent(saved, "ticket.created");

                return toResponse(saved);
        }

        // ============================================================
        // UPDATE
        // ============================================================

        public TicketResponse update(
                        Long id,
                        TicketRequest request) {

                Ticket ticket = ticketRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found with id: " + id));
                Sprint previousSprint = ticket.getSprint();

                projectAccessService.requireEditor(ticket.getProject());
                // Developers can view every ticket on the project board, but they may
                // edit only tickets currently assigned to themselves.
                projectAccessService.requireDeveloperAssignment(
                                ticket.getProject(),
                                ticket.getResponsible() == null ? null : ticket.getResponsible().getId());
                if (request.getSecurityLevel() != null && !projectAccessService.canManage(ticket.getProject()))
                        throw new RuntimeException("Only project admins can change issue security");

                Project project = projectRepository
                                .findById(request.getProjectId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found with id: "
                                                                + request.getProjectId()));
                if (!ticket.getProject().getId().equals(project.getId())) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "A ticket cannot be moved to another project through update");
                }
                projectAccessService.requireEditor(project);
                validateProjectActive(project);

                User owner = userRepository
                                .findById(request.getOwnerId())
                                .filter(user -> user.getDeletedAt() == null)
                                .orElseThrow(() -> new RuntimeException(
                                                "Owner not found with id: "
                                                                + request.getOwnerId()));

                User responsible = null;

                if (request.getResponsibleId() != null) {

                        responsible = userRepository
                                        .findById(request.getResponsibleId())
                                        .filter(user -> user.getDeletedAt() == null)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Responsible user not found with id: "
                                                                        + request.getResponsibleId()));
                }

                validateTicketParticipantBelongsToProject(owner, project, "Owner");
                validateTicketParticipantBelongsToProject(responsible, project, "Responsible user");

                if (projectAccessService.isDeveloper(ticket.getProject())) {
                        Long currentUserId = currentUserService.getCurrentUserId();
                        if (ticket.getResponsible() == null || currentUserId == null
                                        || !currentUserId.equals(ticket.getResponsible().getId())) {
                                throw new org.springframework.security.access.AccessDeniedException(
                                                "Developers can edit only tickets assigned to themselves");
                        }
                        if (request.getResponsibleId() == null
                                        || !currentUserId.equals(request.getResponsibleId())) {
                                throw new org.springframework.security.access.AccessDeniedException(
                                                "Developers cannot reassign their tickets");
                        }
                }

                TicketStatus status = ticketStatusRepository
                                .findById(request.getStatusId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with id: "
                                                                + request.getStatusId()));
                validateStatusBelongsToProject(status, project);
                TicketStatus oldStatus = ticket.getStatus();
                User previousResponsible = ticket.getResponsible();
                Map<String, Object> before = ticketSnapshot(ticket);

                TicketType type = ticketTypeRepository
                                .findById(request.getTypeId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket type not found with id: "
                                                                + request.getTypeId()));
                validateTypeActive(type);

                TicketPriority priority = ticketPriorityRepository
                                .findById(request.getPriorityId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket priority not found with id: "
                                                                + request.getPriorityId()));
                validatePriorityActive(priority);

                Epic epic = null;

                if (request.getEpicId() != null) {

                        epic = epicRepository
                                        .findById(request.getEpicId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Epic not found with id: "
                                                                        + request.getEpicId()));

                        validateEpicBelongsToProject(
                                        epic,
                                        project);
                }

                Ticket parent = resolveParent(request.getParentId(), project, ticket.getId());

                Sprint sprint = resolveSprint(request.getSprintId(), project);
                Milestone milestone = resolveMilestone(request.getMilestoneId(), project);
                Set<Label> labels = resolveLabels(request.getLabelIds(), project);

                // Ticket code is immutable after creation.
                // Do not read a code from the update request and never call
                // ticket.setCode(...).

                ticket.setName(request.getName().trim());
                ticket.setContent(request.getContent());
                ticket.setOwner(owner);
                ticket.setResponsible(responsible);
                if (!java.util.Objects.equals(ticket.getDueDate(), request.getDueDate())
                                || (oldStatus != null && oldStatus.getCategory() != null
                                                && (oldStatus.getCategory() == TicketStatusCategory.DONE
                                                                || oldStatus.getCategory() == TicketStatusCategory.CANCELLED)
                                                && status.getCategory() != null
                                                && status.getCategory() != TicketStatusCategory.DONE
                                                && status.getCategory() != TicketStatusCategory.CANCELLED)) {
                        ticket.setOverdueNotifiedAt(null);
                }
                ticket.setDueDate(request.getDueDate());
                ticket.setStatus(status);
                applyStatusChange(ticket, oldStatus, status);
                ticket.setProject(project);
                ticket.setType(type);

                ticket.setOrder(
                                request.getOrder() != null
                                                ? request.getOrder()
                                                : 0);

                ticket.setPriority(priority);

                ticket.setEstimation(
                                request.getEstimation() != null
                                                ? request.getEstimation()
                                                : BigDecimal.ZERO);

                ticket.setEpic(epic);
                ticket.setParent(parent);
                ticket.setSprint(sprint);
                ticket.setMilestone(milestone);
                ticket.setLabels(labels);
                if (request.getSecurityLevel() != null)
                        ticket.setSecurityLevel(request.getSecurityLevel());

                Ticket updated = ticketRepository.save(ticket);
                syncSprintCommitment(previousSprint, updated);
                customFieldValueService.replaceValues(updated, request.getCustomFields(), project.getId(),
                                type.getId());
                if (responsible != null && (previousResponsible == null
                                || !previousResponsible.getId().equals(responsible.getId()))) {
                        ticketNotificationService.notifyAssignment(updated, currentUserService.getCurrentUser());
                }
                auditService.record(updated.getProject(), updated, "TICKET_UPDATED", "TICKET", updated.getId(),
                                changes(before, ticketSnapshot(updated)));
                publishTicketEvent(updated, "ticket.updated");

                return toResponse(updated);
        }

        public TicketResponse transition(Long id, TicketTransitionRequest request) {
                Ticket ticket = ticketRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
                projectAccessService.requireBoardTransitionAccess(
                                ticket.getProject(),
                                ticket.getOwner() == null ? null : ticket.getOwner().getId(),
                                ticket.getResponsible() == null ? null : ticket.getResponsible().getId());
                TicketStatus status = ticketStatusRepository.findById(request.getStatusId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with id: " + request.getStatusId()));
                validateStatusBelongsToProject(status, ticket.getProject());
                TicketStatus oldStatus = ticket.getStatus();
                workflowService.validateTransition(ticket, oldStatus, status);
                boardService.enforceWip(ticket, status);
                applyStatusChange(ticket, oldStatus, status);
                ticket.setStatus(status);
                workflowService.applyTransitionPostFunctions(ticket, oldStatus, status);
                Ticket updated = ticketRepository.save(ticket);
                boardService.recordTransition(updated, oldStatus, status);
                Map<String, Object> transition = new LinkedHashMap<>();
                transition.put("fromStatusId", oldStatus == null ? null : oldStatus.getId());
                transition.put("toStatusId", status.getId());
                auditService.record(updated.getProject(), updated, "TICKET_TRANSITIONED", "TICKET", updated.getId(),
                                transition);
                publishTicketEvent(updated, "ticket.transitioned");
                return toResponse(updated);
        }

        /**
         * Moves and ranks issues atomically. It powers backlog grooming and Kanban
         * drag/drop.
         * The supplied order becomes 0..N within the selected status/sprint context.
         */
        public List<TicketResponse> plan(Long projectId, TicketPlanningRequest request) {
                Project project = getProject(projectId);
                projectAccessService.requireBoardPlanningAccess(project);
                if (new HashSet<>(request.getTicketIds()).size() != request.getTicketIds().size())
                        throw new RuntimeException("Ticket IDs must not contain duplicates");
                if (request.getSprintId() != null && Boolean.TRUE.equals(request.getMoveToBacklog()))
                        throw new RuntimeException("Specify a sprint or moveToBacklog, not both");

                TicketStatus targetStatus = null;
                if (request.getStatusId() != null) {
                        targetStatus = ticketStatusRepository.findById(request.getStatusId())
                                        .orElseThrow(() -> new RuntimeException("Ticket status not found"));
                        validateStatusBelongsToProject(targetStatus, project);
                }
                Sprint targetSprint = null;
                if (request.getSprintId() != null) {
                        targetSprint = sprintRepository.findByIdAndProjectId(request.getSprintId(), projectId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Sprint does not belong to the selected project"));
                        if (targetSprint.getStatus() == SprintStatus.COMPLETED
                                        || targetSprint.getStatus() == SprintStatus.CANCELLED)
                                throw new RuntimeException("Cannot plan tickets in a completed or cancelled sprint");
                }

                List<Ticket> tickets = new ArrayList<>();
                Map<Long, Sprint> previousSprints = new LinkedHashMap<>();
                for (Long ticketId : request.getTicketIds()) {
                        Ticket ticket = ticketRepository.findById(ticketId)
                                        .filter(item -> item.getDeletedAt() == null)
                                        .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
                        if (!ticket.getProject().getId().equals(projectId))
                                throw new RuntimeException("All tickets must belong to the selected project");
                        previousSprints.put(ticketId, ticket.getSprint());
                        if (targetStatus != null) {
                                TicketStatus oldStatus = ticket.getStatus();
                                ticket.setStatus(targetStatus);
                                applyStatusChange(ticket, oldStatus, targetStatus);
                        }
                        if (targetSprint != null)
                                ticket.setSprint(targetSprint);
                        if (Boolean.TRUE.equals(request.getMoveToBacklog()))
                                ticket.setSprint(null);
                        ticket.setOrder(tickets.size());
                        tickets.add(ticket);
                }
                List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
                for (Ticket savedTicket : savedTickets) {
                        syncSprintCommitment(previousSprints.get(savedTicket.getId()), savedTicket);
                }
                return savedTickets.stream().map(this::toResponse).toList();
        }

        // ============================================================
        // SOFT DELETE
        // ============================================================

        public void delete(Long id) {

                Ticket ticket = ticketRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found with id: " + id));
                projectAccessService.requireManager(ticket.getProject());

                ticket.setDeletedAt(LocalDateTime.now());

                ticketRepository.save(ticket);
                auditService.record(ticket.getProject(), ticket, "TICKET_DELETED", "TICKET", ticket.getId(), Map.of());
                publishTicketEvent(ticket, "ticket.deleted");
        }

        // ============================================================
        // RESTORE
        // ============================================================

        public TicketResponse restore(Long id) {

                Ticket ticket = ticketRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found with id: " + id));
                projectAccessService.requireManager(ticket.getProject());

                ticket.setDeletedAt(null);

                Ticket restored = ticketRepository.save(ticket);
                auditService.record(restored.getProject(), restored, "TICKET_RESTORED", "TICKET", restored.getId(),
                                Map.of());
                publishTicketEvent(restored, "ticket.restored");
                return toResponse(restored);
        }

        // ============================================================
        // PERMANENT DELETE
        // ============================================================

        public void permanentDelete(Long id) {

                Ticket ticket = ticketRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
                projectAccessService.requireManager(ticket.getProject());
                auditService.record(ticket.getProject(), ticket, "TICKET_PERMANENTLY_DELETED", "TICKET", ticket.getId(),
                                ticketSnapshot(ticket));
                ticketRepository.delete(ticket);
        }

        // ============================================================
        // GENERATE TICKET CODE
        // ============================================================

        private void validateTicketParticipantBelongsToProject(User user, Project project, String label) {
                if (user == null || project == null) {
                        return;
                }
                boolean member = projectUserRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
                if (!member) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        label + " must be a member of the selected project");
                }
        }

        private Project getProject(Long projectId) {
                return projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        }

        private String blankToNull(String value) {
                return value == null || value.isBlank() ? null : value.trim();
        }

        private Map<String, Object> ticketSnapshot(Ticket ticket) {
                Map<String, Object> values = new LinkedHashMap<>();
                values.put("name", ticket.getName());
                values.put("statusId", ticket.getStatus() == null ? null : ticket.getStatus().getId());
                values.put("priorityId", ticket.getPriority() == null ? null : ticket.getPriority().getId());
                values.put("responsibleId", ticket.getResponsible() == null ? null : ticket.getResponsible().getId());
                values.put("sprintId", ticket.getSprint() == null ? null : ticket.getSprint().getId());
                values.put("epicId", ticket.getEpic() == null ? null : ticket.getEpic().getId());
                values.put("parentId", ticket.getParent() == null ? null : ticket.getParent().getId());
                values.put("estimation", ticket.getEstimation());
                values.put("order", ticket.getOrder());
                return values;
        }

        private Map<String, Object> changes(Map<String, Object> before, Map<String, Object> after) {
                Map<String, Object> changed = new LinkedHashMap<>();
                for (String key : before.keySet())
                        if (!java.util.Objects.equals(before.get(key), after.get(key)))
                                changed.put(key, Map.of("from", before.get(key) == null ? "" : before.get(key), "to",
                                                after.get(key) == null ? "" : after.get(key)));
                return changed;
        }

        private void publishTicketEvent(Ticket ticket, String type) {
                realtimeEvents.publishProject(ticket.getProject().getId(), type,
                                Map.of("ticketId", ticket.getId(), "ticketCode", ticket.getCode()));
        }

        private boolean canView(Ticket ticket) {
                if (!projectAccessService.canView(ticket.getProject()))
                        return false;
                IssueSecurityLevel level = ticket.getSecurityLevel() == null ? IssueSecurityLevel.PROJECT
                                : ticket.getSecurityLevel();
                if (level == IssueSecurityLevel.PROJECT || level == IssueSecurityLevel.MEMBERS)
                        return true;
                Long uid = currentUserService.getCurrentUserId();
                if (uid == null)
                        return false;
                if (level == IssueSecurityLevel.ASSIGNEE)
                        return ticket.getResponsible() != null && uid.equals(ticket.getResponsible().getId());
                if (level == IssueSecurityLevel.REPORTER)
                        return ticket.getOwner() != null && uid.equals(ticket.getOwner().getId());
                return false;
        }

        private String generateTicketCode(
                        Project project) {

                String prefix = project.getTicketPrefix();

                if (prefix == null || prefix.isBlank()) {
                        prefix = "TICKET";
                }

                prefix = prefix
                                .trim()
                                .toUpperCase()
                                .replaceAll("[^A-Z0-9_-]", "");

                if (prefix.isBlank()) {
                        prefix = "TICKET";
                }

                long count = ticketRepository.countByProjectId(
                                project.getId());

                String code;

                do {

                        count++;

                        code = prefix + "-" + count;

                } while (ticketRepository.existsByCode(code));

                return code;
        }

        // ============================================================
        // VALIDATE EPIC PROJECT
        // ============================================================

        private void validateEpicBelongsToProject(
                        Epic epic,
                        Project project) {

                if (epic.getDeletedAt() != null) {
                        throw new RuntimeException("Epic is deleted");
                }
                if (epic.getProject() == null ||
                                !epic.getProject()
                                                .getId()
                                                .equals(project.getId())) {

                        throw new RuntimeException(
                                        "Epic does not belong to the selected project");
                }
        }

        private void validateProjectActive(Project project) {
                if (project.getDeletedAt() != null) {
                        throw new RuntimeException("Project is deleted");
                }
        }

        private void validateTypeActive(TicketType type) {
                if (type.getDeletedAt() != null) {
                        throw new RuntimeException("Ticket type is deleted");
                }
        }

        private void validatePriorityActive(TicketPriority priority) {
                if (priority.getDeletedAt() != null) {
                        throw new RuntimeException("Ticket priority is deleted");
                }
        }

        private void validateStatusBelongsToProject(TicketStatus status, Project project) {
                if (status.getDeletedAt() != null) {
                        throw new RuntimeException("Ticket status is deleted");
                }
                if (status.getProject() != null && !status.getProject().getId().equals(project.getId())) {
                        throw new RuntimeException("Ticket status does not belong to the selected project");
                }
        }

        private boolean isTerminalStatus(TicketStatus status) {
                return status.getCategory() == TicketStatusCategory.DONE
                                || status.getCategory() == TicketStatusCategory.CANCELLED;
        }

        private void applyStatusChange(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
                if (oldStatus != null && oldStatus.getId().equals(newStatus.getId()))
                        return;
                ticket.setResolvedAt(isTerminalStatus(newStatus) ? LocalDateTime.now() : null);
                ticketActivityRepository
                                .save(TicketActivity.builder().ticket(ticket).oldStatus(oldStatus).newStatus(newStatus)
                                                .user(currentUserService.getCurrentUser()).build());
                ticketNotificationService.notifyStatusChange(ticket, currentUserService.getCurrentUser(), oldStatus,
                                newStatus);
        }

        /**
         * Keeps sprint_issue_snapshots aligned with the current committed sprint scope.
         * ACTIVE sprint: add when entering, remove when leaving.
         * PLANNED sprint: start() owns initial snapshot creation.
         */
        private void syncSprintCommitment(Sprint previousSprint, Ticket ticket) {
                Sprint newSprint = ticket.getSprint();

                if (previousSprint != null && !java.util.Objects.equals(
                                previousSprint.getId(), newSprint == null ? null : newSprint.getId())) {
                        snapshotRepository.findBySprintIdAndTicketId(previousSprint.getId(), ticket.getId())
                                        .ifPresent(snapshotRepository::delete);
                }

                if (newSprint != null && newSprint.getStatus() == SprintStatus.ACTIVE
                                && !snapshotRepository.existsBySprintIdAndTicketId(newSprint.getId(), ticket.getId())) {
                        snapshotRepository.save(SprintIssueSnapshot.builder()
                                        .sprint(newSprint)
                                        .ticketId(ticket.getId())
                                        .estimation(ticket.getEstimation() == null ? BigDecimal.ZERO
                                                        : ticket.getEstimation())
                                        .resolvedAt(ticket.getResolvedAt())
                                        .finalStatusCategory(ticket.getStatus() == null
                                                        ? TicketStatusCategory.TODO
                                                        : ticket.getStatus().getCategory())
                                        .build());
                }
        }

        private Sprint resolveSprint(Long sprintId, Project project) {
                if (sprintId == null)
                        return null;
                Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, project.getId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Sprint does not belong to the selected project"));
                if (sprint.getStatus() == com.projectmanagement.app.sprint.SprintStatus.COMPLETED
                                || sprint.getStatus() == com.projectmanagement.app.sprint.SprintStatus.CANCELLED) {
                        throw new RuntimeException("Cannot add a ticket to a completed or cancelled sprint");
                }
                return sprint;
        }

        private Ticket resolveParent(Long parentId, Project project, Long ticketId) {
                if (parentId == null)
                        return null;
                if (parentId.equals(ticketId))
                        throw new RuntimeException("A ticket cannot be its own parent");
                Ticket parent = ticketRepository.findById(parentId)
                                .filter(ticket -> ticket.getDeletedAt() == null)
                                .orElseThrow(() -> new RuntimeException("Parent ticket not found"));
                if (!parent.getProject().getId().equals(project.getId()))
                        throw new RuntimeException("Parent ticket must belong to the same project");
                if (ticketId != null && isDescendantOf(parent, ticketId))
                        throw new RuntimeException("A ticket cannot be moved below one of its descendants");
                return parent;
        }

        private boolean isDescendantOf(Ticket candidate, Long ancestorId) {
                Ticket current = candidate;
                while (current != null) {
                        if (current.getId().equals(ancestorId))
                                return true;
                        current = current.getParent();
                }
                return false;
        }

        private Milestone resolveMilestone(Long milestoneId, Project project) {
                if (milestoneId == null)
                        return null;
                return milestoneRepository.findByIdAndProjectId(milestoneId, project.getId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Milestone does not belong to the selected project"));
        }

        private Set<Label> resolveLabels(Set<Long> labelIds, Project project) {
                Set<Label> labels = new HashSet<>();
                if (labelIds == null || labelIds.isEmpty())
                        return labels;
                for (Long labelId : labelIds) {
                        labels.add(labelRepository.findByIdAndProjectId(labelId, project.getId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Label does not belong to the selected project: " + labelId)));
                }
                return labels;
        }

        // ============================================================
        // ENTITY -> RESPONSE
        // ============================================================

        public void remindAssignee(Long id) {
                Ticket ticket = ticketRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));

                projectAccessService.requireManager(ticket.getProject());

                if (ticket.getResponsible() == null) {
                        throw new RuntimeException("This task has no assigned person");
                }

                ticketNotificationService.notifyManualReminder(
                                ticket,
                                currentUserService.getCurrentUser());
        }

        public TicketResponse toResponse(
                        Ticket ticket) {

                Long ownerId = null;
                String ownerName = null;
                String ownerEmail = null;

                if (ticket.getOwner() != null) {

                        ownerId = ticket.getOwner().getId();
                        ownerName = ticket.getOwner().getName();
                        ownerEmail = ticket.getOwner().getEmail();
                }

                Long responsibleId = null;
                String responsibleName = null;
                String responsibleEmail = null;

                if (ticket.getResponsible() != null) {

                        responsibleId = ticket.getResponsible().getId();
                        responsibleName = ticket.getResponsible().getName();
                        responsibleEmail = ticket.getResponsible().getEmail();
                }

                Long statusId = null;
                String statusName = null;
                String statusColor = null;
                TicketStatusCategory statusCategory = null;

                if (ticket.getStatus() != null) {

                        statusId = ticket.getStatus().getId();
                        statusName = ticket.getStatus().getName();
                        statusColor = ticket.getStatus().getColor();
                        statusCategory = ticket.getStatus().getCategory();
                }

                Long projectId = null;
                String projectName = null;

                if (ticket.getProject() != null) {

                        projectId = ticket.getProject().getId();
                        projectName = ticket.getProject().getName();
                }

                Long typeId = null;
                String typeName = null;
                String typeIcon = null;
                String typeColor = null;

                if (ticket.getType() != null) {

                        typeId = ticket.getType().getId();
                        typeName = ticket.getType().getName();
                        typeIcon = ticket.getType().getIcon();
                        typeColor = ticket.getType().getColor();
                }

                Long priorityId = null;
                String priorityName = null;
                String priorityColor = null;

                if (ticket.getPriority() != null) {

                        priorityId = ticket.getPriority().getId();
                        priorityName = ticket.getPriority().getName();
                        priorityColor = ticket.getPriority().getColor();
                }

                Long epicId = null;
                String epicName = null;

                if (ticket.getEpic() != null) {

                        epicId = ticket.getEpic().getId();
                        epicName = ticket.getEpic().getName();
                }

                Long parentId = ticket.getParent() == null ? null : ticket.getParent().getId();
                String parentCode = ticket.getParent() == null ? null : ticket.getParent().getCode();
                String parentName = ticket.getParent() == null ? null : ticket.getParent().getName();
                long childCount = ticketRepository.countByParentIdAndDeletedAtIsNull(ticket.getId());

                Long sprintId = ticket.getSprint() == null ? null : ticket.getSprint().getId();
                String sprintName = ticket.getSprint() == null ? null : ticket.getSprint().getName();
                Long milestoneId = ticket.getMilestone() == null ? null : ticket.getMilestone().getId();
                String milestoneName = ticket.getMilestone() == null ? null : ticket.getMilestone().getName();
                Long releaseId = ticket.getRelease() == null ? null : ticket.getRelease().getId();
                String releaseVersion = ticket.getRelease() == null ? null : ticket.getRelease().getVersion();
                Set<Long> labelIds = ticket.getLabels().stream().map(Label::getId)
                                .collect(java.util.stream.Collectors.toSet());

                return TicketResponse.builder()
                                .id(ticket.getId())
                                .name(ticket.getName())
                                .content(ticket.getContent())

                                .ownerId(ownerId)
                                .ownerName(ownerName)
                                .ownerEmail(ownerEmail)

                                .responsibleId(responsibleId)
                                .responsibleName(responsibleName)
                                .responsibleEmail(responsibleEmail)

                                .statusId(statusId)
                                .statusName(statusName)
                                .statusColor(statusColor)
                                .statusCategory(statusCategory)

                                .projectId(projectId)
                                .projectName(projectName)

                                .code(ticket.getCode())

                                .typeId(typeId)
                                .typeName(typeName)
                                .typeIcon(typeIcon)
                                .typeColor(typeColor)

                                .order(ticket.getOrder())

                                .priorityId(priorityId)
                                .priorityName(priorityName)
                                .priorityColor(priorityColor)

                                .estimation(ticket.getEstimation())

                                .epicId(epicId)
                                .epicName(epicName)

                                .parentId(parentId)
                                .parentCode(parentCode)
                                .parentName(parentName)
                                .childCount(childCount)

                                .sprintId(sprintId)
                                .sprintName(sprintName)
                                .milestoneId(milestoneId)
                                .milestoneName(milestoneName)
                                .releaseId(releaseId)
                                .releaseVersion(releaseVersion)
                                .labelIds(labelIds)
                                .securityLevel(ticket.getSecurityLevel())
                                .customFields(customFieldValueService.getValues(ticket.getId()))

                                .deletedAt(ticket.getDeletedAt())
                                .resolvedAt(ticket.getResolvedAt())
                                .dueDate(ticket.getDueDate())
                                .overdueNotifiedAt(ticket.getOverdueNotifiedAt())
                                .createdAt(ticket.getCreatedAt())
                                .updatedAt(ticket.getUpdatedAt())

                                .build();
        }
}
