package com.projectmanagement.app.project;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.audit.AuditService;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
public class ProjectService {

        private final ProjectRepository projectRepository;
        private final UserRepository userRepository;
        private final ProjectStatusRepository projectStatusRepository;
        private final CurrentUserService currentUserService;
        private final ProjectAccessService projectAccessService;
        private final ProjectUserRepository projectUserRepository;
        private final AuditService auditService;

        public ProjectService(
                        ProjectRepository projectRepository,
                        UserRepository userRepository,
                        ProjectStatusRepository projectStatusRepository,
                        CurrentUserService currentUserService,
                        ProjectAccessService projectAccessService,
                        ProjectUserRepository projectUserRepository,
                        AuditService auditService) {

                this.projectRepository = projectRepository;
                this.userRepository = userRepository;
                this.projectStatusRepository = projectStatusRepository;
                this.currentUserService = currentUserService;
                this.projectAccessService = projectAccessService;
                this.projectUserRepository = projectUserRepository;
                this.auditService = auditService;
        }

        // ============================================================
        // GET ALL PROJECTS
        // ============================================================

        @Transactional(readOnly = true)
        public List<ProjectResponse> getAllProjects() {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (isAdmin(authentication)) {
                        return projectRepository.findAll()
                                        .stream()
                                        .map(this::toResponse)
                                        .toList();
                }

                Long userId = currentUserService.getCurrentUserId();

                return projectRepository.findVisibleProjectsForUser(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET ALL ACTIVE PROJECTS
        // ============================================================

        @Transactional(readOnly = true)
        public List<ProjectResponse> getAllActiveProjects() {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (isAdmin(authentication)) {
                        return projectRepository.findByArchivedAtIsNullAndDeletedAtIsNull()
                                        .stream()
                                        .map(this::toResponse)
                                        .toList();
                }

                Long userId = currentUserService.getCurrentUserId();

                return projectRepository.findVisibleActiveProjectsForUser(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET PROJECT BY ID
        // ============================================================

        @Transactional(readOnly = true)
        public ProjectResponse getProjectById(Long id) {

                validateId(id);

                Project project = getProjectEntityById(id);

                projectAccessService.requireView(project);

                return toResponse(project);
        }

        // ============================================================
        // GET PROJECTS BY OWNER
        // ============================================================

        @Transactional(readOnly = true)
        public List<ProjectResponse> getProjectsByOwner(Long ownerId) {

                validateUserId(ownerId);

                return projectRepository.findByOwnerId(ownerId)
                                .stream()
                                .filter(this::canViewProject)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET ACTIVE PROJECTS BY OWNER
        // ============================================================

        @Transactional(readOnly = true)
        public List<ProjectResponse> getActiveProjectsByOwner(Long ownerId) {

                validateUserId(ownerId);

                return projectRepository.findByOwnerIdAndArchivedAtIsNullAndDeletedAtIsNull(ownerId)
                                .stream()
                                .filter(this::canViewProject)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET PROJECTS BY STATUS
        // ============================================================

        @Transactional(readOnly = true)
        public List<ProjectResponse> getProjectsByStatus(Long statusId) {

                validateStatusId(statusId);

                return projectRepository.findByStatusId(statusId)
                                .stream()
                                .filter(this::canViewProject)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET ACTIVE PROJECTS BY STATUS
        // ============================================================

        @Transactional(readOnly = true)
        public List<ProjectResponse> getActiveProjectsByStatus(Long statusId) {

                validateStatusId(statusId);

                return projectRepository.findByStatusIdAndArchivedAtIsNullAndDeletedAtIsNull(statusId)
                                .stream()
                                .filter(this::canViewProject)
                                .map(this::toResponse)
                                .toList();
        }

        // ============================================================
        // GET PROJECT BY NAME
        // ============================================================

        @Transactional(readOnly = true)
        public ProjectResponse getProjectByName(String name) {

                validateName(name);

                Project project = projectRepository
                                .findByNameAndDeletedAtIsNull(name.trim())
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found with name: " + name));

                projectAccessService.requireView(project);

                return toResponse(project);
        }

        // ============================================================
        // CREATE PROJECT
        // ============================================================

        @Transactional
        public ProjectResponse createProject(ProjectRequest request) {

                validateRequest(request);

                String normalizedName = request.getName().trim();
                String normalizedPrefix = request.getTicketPrefix().trim();

                if (projectRepository.existsByName(normalizedName)) {
                        throw new RuntimeException(
                                        "Project already exists with name: " + normalizedName);
                }

                if (projectRepository.existsByTicketPrefix(normalizedPrefix)) {
                        throw new RuntimeException(
                                        "Ticket prefix already exists: " + normalizedPrefix);
                }

                User owner = getUserById(request.getOwnerId());

                ProjectStatus status = getStatusById(request.getStatusId());

                Project project = Project.builder()
                                .name(normalizedName)
                                .description(request.getDescription())
                                .owner(owner)
                                .status(status)
                                .ticketPrefix(normalizedPrefix)
                                .statusType(
                                                request.getStatusType() != null
                                                                && !request.getStatusType().isBlank()
                                                                                ? request.getStatusType().trim()
                                                                                : "default")
                                .build();

                Project savedProject = projectRepository.save(project);

                // The creator/owner is the project administrator for the new project.
                if (!projectUserRepository.existsByProjectIdAndUserId(savedProject.getId(), owner.getId())) {
                        projectUserRepository.save(ProjectUser.builder()
                                        .project(savedProject)
                                        .user(owner)
                                        .role(ProjectRole.PROJECT_ADMIN.name())
                                        .build());
                }

                auditService.record(savedProject, null, "PROJECT_CREATED", "PROJECT", savedProject.getId(),
                                java.util.Map.of("name", savedProject.getName(), "ticketPrefix",
                                                savedProject.getTicketPrefix()));

                return toResponse(savedProject);
        }

        // ============================================================
        // UPDATE PROJECT
        // ============================================================

        @Transactional
        public ProjectResponse updateProject(
                        Long id,
                        ProjectRequest request) {

                validateId(id);
                validateRequest(request);

                Project project = getProjectEntityById(id);

                projectAccessService.requireManager(project);

                if (project.getArchivedAt() != null
                                && !isAdmin(SecurityContextHolder.getContext().getAuthentication())) {
                        throw new RuntimeException("Unarchive the project before changing project settings");
                }

                if (!isAdmin(SecurityContextHolder.getContext().getAuthentication())
                                && !project.getOwner().getId().equals(currentUserService.getCurrentUserId())
                                && request.getOwnerId() != null
                                && !project.getOwner().getId().equals(request.getOwnerId())) {
                        throw new RuntimeException(
                                        "Only the system ADMIN or project owner can change project ownership");
                }

                String normalizedName = request.getName().trim();
                String normalizedPrefix = request.getTicketPrefix().trim();

                if (projectRepository.existsByNameAndIdNot(
                                normalizedName,
                                id)) {

                        throw new RuntimeException(
                                        "Project already exists with name: "
                                                        + normalizedName);
                }

                if (projectRepository.existsByTicketPrefixAndIdNot(
                                normalizedPrefix,
                                id)) {

                        throw new RuntimeException(
                                        "Ticket prefix already exists: "
                                                        + normalizedPrefix);
                }

                User owner = getUserById(request.getOwnerId());

                ProjectStatus status = getStatusById(request.getStatusId());

                project.setName(normalizedName);
                project.setDescription(request.getDescription());
                project.setOwner(owner);
                project.setStatus(status);
                project.setTicketPrefix(normalizedPrefix);

                if (request.getStatusType() != null
                                && !request.getStatusType().isBlank()) {

                        project.setStatusType(
                                        request.getStatusType().trim());
                }

                Project updatedProject = projectRepository.save(project);

                auditService.record(updatedProject, null, "PROJECT_UPDATED", "PROJECT", updatedProject.getId(),
                                java.util.Map.of("name", updatedProject.getName(), "ticketPrefix",
                                                updatedProject.getTicketPrefix(),
                                                "statusId", updatedProject.getStatus().getId()));

                return toResponse(updatedProject);
        }

        // ============================================================
        // SOFT DELETE PROJECT
        // ============================================================

        @Transactional
        public void deleteProject(Long id) {

                validateId(id);

                Project project = getProjectEntityById(id);

                projectAccessService.requireManager(project);

                if (project.getArchivedAt() != null) {
                        throw new RuntimeException("Unarchive the project before deleting it");
                }

                project.setDeletedAt(LocalDateTime.now());

                projectRepository.save(project);
                auditService.record(project, null, "PROJECT_DELETED", "PROJECT", project.getId(), java.util.Map.of());
        }

        // ============================================================
        // RESTORE PROJECT
        // ============================================================

        @Transactional
        public void restoreProject(Long id) {

                validateId(id);

                Project project = getProjectEntityById(id);

                projectAccessService.requireManager(project);

                project.setDeletedAt(null);

                projectRepository.save(project);
                auditService.record(project, null, "PROJECT_RESTORED", "PROJECT", project.getId(), java.util.Map.of());
        }

        // ============================================================
        // PERMANENTLY DELETE PROJECT
        // ============================================================

        @Transactional
        public void permanentlyDeleteProject(Long id) {

                validateId(id);

                Project project = getProjectEntityById(id);

                projectAccessService.requireManager(project);

                projectRepository.delete(project);
        }

        private boolean canViewProject(Project project) {

                try {
                        return projectAccessService.canView(project);
                } catch (RuntimeException ex) {
                        return false;
                }
        }

        // ============================================================
        // GET PROJECT ENTITY
        // ============================================================

        private Project getProjectEntityById(Long id) {

                return projectRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found with id: " + id));
        }

        // ============================================================
        // GET USER
        // ============================================================

        private User getUserById(Long userId) {

                return userRepository
                                .findById(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with id: " + userId));
        }

        // ============================================================
        // GET PROJECT STATUS
        // ============================================================

        private ProjectStatus getStatusById(
                        Long statusId) {

                return projectStatusRepository
                                .findById(statusId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project status not found with id: "
                                                                + statusId));
        }

        // ============================================================
        // CONVERT ENTITY TO RESPONSE
        // ============================================================

        private ProjectResponse toResponse(
                        Project project) {

                Long ownerId = null;
                String ownerName = null;
                String ownerEmail = null;

                if (project.getOwner() != null) {

                        ownerId = project.getOwner().getId();
                        ownerName = project.getOwner().getName();
                        ownerEmail = project.getOwner().getEmail();
                }

                Long statusId = null;
                String statusName = null;
                String statusColor = null;

                if (project.getStatus() != null) {

                        statusId = project.getStatus().getId();
                        statusName = project.getStatus().getName();
                        statusColor = project.getStatus().getColor();
                }

                return ProjectResponse.builder()
                                .id(project.getId())
                                .name(project.getName())
                                .description(project.getDescription())

                                .ownerId(ownerId)
                                .ownerName(ownerName)
                                .ownerEmail(ownerEmail)

                                .statusId(statusId)
                                .statusName(statusName)
                                .statusColor(statusColor)

                                .ticketPrefix(project.getTicketPrefix())
                                .statusType(project.getStatusType())

                                .deletedAt(project.getDeletedAt())
                                .archivedAt(project.getArchivedAt())
                                .createdAt(project.getCreatedAt())
                                .updatedAt(project.getUpdatedAt())

                                .build();
        }

        // ============================================================
        // VALIDATE PROJECT REQUEST
        // ============================================================

        private void validateRequest(
                        ProjectRequest request) {

                if (request == null) {
                        throw new IllegalArgumentException(
                                        "Project request cannot be null");
                }

                validateName(request.getName());
                validateTicketPrefix(request.getTicketPrefix());
                validateUserId(request.getOwnerId());
                validateStatusId(request.getStatusId());
        }

        // ============================================================
        // VALIDATE PROJECT ID
        // ============================================================

        private void validateId(Long id) {

                if (id == null || id <= 0) {

                        throw new IllegalArgumentException(
                                        "Project ID must be greater than zero");
                }
        }

        // ============================================================
        // VALIDATE USER ID
        // ============================================================

        private void validateUserId(Long userId) {

                if (userId == null || userId <= 0) {

                        throw new IllegalArgumentException(
                                        "Owner ID must be greater than zero");
                }
        }

        // ============================================================
        // VALIDATE STATUS ID
        // ============================================================

        private void validateStatusId(Long statusId) {

                if (statusId == null || statusId <= 0) {

                        throw new IllegalArgumentException(
                                        "Status ID must be greater than zero");
                }
        }

        // ============================================================
        // VALIDATE PROJECT NAME
        // ============================================================

        private void validateName(String name) {

                if (name == null || name.isBlank()) {

                        throw new IllegalArgumentException(
                                        "Project name cannot be empty");
                }
        }

        // ============================================================
        // VALIDATE TICKET PREFIX
        // ============================================================

        private void validateTicketPrefix(
                        String ticketPrefix) {

                if (ticketPrefix == null
                                || ticketPrefix.isBlank()) {

                        throw new IllegalArgumentException(
                                        "Ticket prefix cannot be empty");
                }
        }

        // ============================================================
        // CHECK ADMIN
        // ============================================================

        private boolean isAdmin(
                        Authentication authentication) {

                if (authentication == null
                                || authentication.getAuthorities() == null) {

                        return false;
                }

                return authentication
                                .getAuthorities()
                                .stream()
                                .anyMatch(authority -> "ROLE_ADMIN".equals(
                                                authority.getAuthority()));
        }
}