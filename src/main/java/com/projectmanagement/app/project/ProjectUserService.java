package com.projectmanagement.app.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.audit.AuditService;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
@Transactional
public class ProjectUserService {

        private final ProjectUserRepository projectUserRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final ProjectAccessService projectAccessService;
        private final AuditService auditService;

        public ProjectUserService(
                        ProjectUserRepository projectUserRepository,
                        ProjectRepository projectRepository,
                        UserRepository userRepository,
                        ProjectAccessService projectAccessService,
                        AuditService auditService) {

                this.projectUserRepository = projectUserRepository;
                this.projectRepository = projectRepository;
                this.userRepository = userRepository;
                this.projectAccessService = projectAccessService;
                this.auditService = auditService;
        }

        // -------------------------------------------------------------------------
        // READ
        // -------------------------------------------------------------------------

        @Transactional(readOnly = true)
        public List<ProjectUserResponse> getAllProjectUsers() {

                return projectUserRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public ProjectUserResponse getProjectUserById(Long id) {

                ProjectUser projectUser = projectUserRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project user not found with id: " + id));

                projectAccessService.requireView(projectUser.getProject());
                return toResponse(projectUser);
        }

        @Transactional(readOnly = true)
        public List<ProjectUserResponse> getProjectUsersByProject(
                        Long projectId) {

                Project project = getProject(projectId);

                projectAccessService.requireView(project);

                return projectUserRepository.findByProjectId(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ProjectUserResponse> getProjectUsersByUser(
                        Long userId) {

                validateUser(userId);

                return projectUserRepository.findByUserId(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ProjectUserResponse> getProjectUsersByProjectAndRole(
                        Long projectId,
                        String role) {

                Project project = getProject(projectId);

                projectAccessService.requireView(project);

                return projectUserRepository
                                .findByProjectIdAndRole(
                                                projectId,
                                                normalizeRole(role))
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public boolean existsByProjectAndUser(
                        Long projectId,
                        Long userId) {

                Project project = getProject(projectId);

                projectAccessService.requireView(project);

                return projectUserRepository.existsByProjectIdAndUserId(
                                projectId,
                                userId);
        }

        // -------------------------------------------------------------------------
        // CREATE
        // -------------------------------------------------------------------------

        public ProjectUserResponse createProjectUser(
                        ProjectUserRequest request) {

                validateRequest(request);

                Project project = getProject(request.getProjectId());

                validateUser(request.getUserId());
                String normalizedRole = normalizeRequestedRole(request);

                // Project membership and project-role assignment are project-level
                // operations. A Project Admin can add/change Project Admin, Team Lead,
                // and Developer memberships in their own project, subject to the
                // 1-2 Project Admin cardinality rule. System Admin may do the same
                // when acting as a system administrator.
                projectAccessService.requireProjectMembershipAdministration(project);

                validateAdminCardinality(project.getId(), normalizedRole, null);

                if (projectUserRepository.existsByProjectIdAndUserId(
                                request.getProjectId(),
                                request.getUserId())) {

                        throw new RuntimeException(
                                        "User is already assigned to this project");
                }

                User user = getUser(request.getUserId());

                ProjectUser projectUser = ProjectUser.builder()
                                .user(user)
                                .project(project)
                                .role(normalizedRole)
                                .responsibilityRole(null)
                                .availabilitySelfUpdateOpen(Boolean.FALSE)
                                .build();

                ProjectUser saved = projectUserRepository.save(projectUser);
                auditService.record(project, null, "PROJECT_MEMBER_ADDED", "PROJECT_USER", saved.getId(),
                                java.util.Map.of("userId", user.getId(), "role", saved.getRole(),
                                                "responsibilityRole", saved.getResponsibilityRole() == null ? ""
                                                                : saved.getResponsibilityRole()));
                return toResponse(saved);
        }

        // -------------------------------------------------------------------------
        // UPDATE
        // -------------------------------------------------------------------------

        public ProjectUserResponse updateProjectUser(
                        Long id,
                        ProjectUserRequest request) {

                validateRequest(request);

                ProjectUser projectUser = projectUserRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project user not found with id: " + id));

                Project oldProject = projectUser.getProject();
                Long oldProjectId = oldProject.getId();

                Project newProject = getProject(request.getProjectId());

                validateUser(request.getUserId());
                String normalizedRole = normalizeRequestedRole(request);
                // Project Admins can change memberships and roles only within
                // projects they administer. Moving a membership between projects
                // requires administration access to both projects.
                projectAccessService.requireProjectMembershipAdministration(oldProject);
                projectAccessService.requireProjectMembershipAdministration(newProject);

                boolean userChanged = !projectUser.getUser().getId()
                                .equals(request.getUserId());

                boolean projectChanged = !projectUser.getProject().getId()
                                .equals(request.getProjectId());

                if ((userChanged || projectChanged)
                                && projectUserRepository
                                                .existsByProjectIdAndUserIdAndIdNot(
                                                                request.getProjectId(),
                                                                request.getUserId(),
                                                                id)) {

                        throw new RuntimeException(
                                        "User is already assigned to this project");
                }

                projectUser.setUser(
                                getUser(request.getUserId()));

                projectUser.setProject(newProject);

                String oldRole = projectUser.getRole();
                if (ProjectRole.PROJECT_ADMIN.name().equalsIgnoreCase(oldRole)
                                && (!ProjectRole.PROJECT_ADMIN.name().equalsIgnoreCase(normalizedRole)
                                                || projectChanged)) {
                        ensureNotLastProjectAdmin(oldProjectId, projectUser.getId());
                }
                validateAdminCardinality(newProject.getId(), normalizedRole, id);

                projectUser.setRole(normalizedRole);
                projectUser.setResponsibilityRole(null);

                /*
                 * Preserve the current availability submission setting.
                 *
                 * If the database contains NULL because this is an older record,
                 * default it to CLOSED.
                 */
                if (projectUser.getAvailabilitySelfUpdateOpen() == null) {
                        projectUser.setAvailabilitySelfUpdateOpen(
                                        Boolean.FALSE);
                }

                ProjectUser saved = projectUserRepository.save(projectUser);
                auditService.record(newProject, null, "PROJECT_MEMBER_UPDATED", "PROJECT_USER", saved.getId(),
                                java.util.Map.of("userId", saved.getUser().getId(), "oldRole",
                                                oldRole == null ? "" : oldRole,
                                                "newRole", saved.getRole(),
                                                "responsibilityRole", saved.getResponsibilityRole() == null ? ""
                                                                : saved.getResponsibilityRole()));
                return toResponse(saved);
        }

        // -------------------------------------------------------------------------
        // AVAILABILITY SELF-UPDATE
        // -------------------------------------------------------------------------

        /**
         * Returns whether the selected project member is allowed
         * to submit/update their own availability.
         */
        @Transactional(readOnly = true)
        public boolean isAvailabilitySelfUpdateOpen(
                        Long projectUserId) {

                ProjectUser projectUser = getProjectUser(projectUserId);

                return Boolean.TRUE.equals(
                                projectUser.getAvailabilitySelfUpdateOpen());
        }

        public ProjectUserResponse setAvailabilitySelfUpdateOpen(
                        Long projectUserId,
                        boolean enabled) {

                ProjectUser projectUser = getProjectUser(projectUserId);

                Project project = projectUser.getProject();

                if (project == null) {
                        throw new RuntimeException(
                                        "Project not found for project user: " + projectUserId);
                }

                projectAccessService.requireManager(project);

                projectUser.setAvailabilitySelfUpdateOpen(enabled);

                ProjectUser savedProjectUser = projectUserRepository.save(projectUser);

                return toResponse(savedProjectUser);
        }

        /**
         * Opens availability submission for a project member.
         */
        public ProjectUserResponse openAvailabilitySelfUpdate(
                        Long projectUserId) {

                return setAvailabilitySelfUpdateOpen(
                                projectUserId,
                                true);
        }

        /**
         * Closes availability submission for a project member.
         *
         * Existing availability records are not deleted.
         */
        public ProjectUserResponse closeAvailabilitySelfUpdate(
                        Long projectUserId) {

                return setAvailabilitySelfUpdateOpen(
                                projectUserId,
                                false);
        }

        // -------------------------------------------------------------------------
        // DELETE
        // -------------------------------------------------------------------------

        public void deleteProjectUser(Long id) {

                ProjectUser projectUser = projectUserRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project user not found with id: "
                                                                + id));

                projectAccessService.requireProjectMembershipAdministration(projectUser.getProject());

                Long userId = projectUser.getUser() != null ? projectUser.getUser().getId() : null;
                if (ProjectRole.PROJECT_ADMIN.name().equalsIgnoreCase(projectUser.getRole())) {
                        ensureNotLastProjectAdmin(projectUser.getProject().getId(), projectUser.getId());
                }
                auditService.record(projectUser.getProject(), null, "PROJECT_MEMBER_REMOVED", "PROJECT_USER",
                                projectUser.getId(),
                                java.util.Map.of("userId", projectUser.getUser().getId(), "role",
                                                projectUser.getRole()));
                projectUserRepository.delete(projectUser);
        }

        public void deleteProjectUsersByProject(
                        Long projectId) {

                Project project = getProject(projectId);

                projectAccessService.requireProjectMembershipAdministration(project);

                // Preserve all Project Admin memberships so the project can never
                // be left without an administrator.
                projectUserRepository.findByProjectId(projectId).stream()
                                .filter(member -> !ProjectRole.PROJECT_ADMIN.name().equalsIgnoreCase(member.getRole()))
                                .forEach(projectUserRepository::delete);
        }

        public void deleteProjectUsersByUser(Long userId) {

                validateUser(userId);

                /*
                 * Global user cleanup remains restricted by
                 * controller/global permission.
                 */
                projectUserRepository.deleteByUserId(userId);
        }

        // -------------------------------------------------------------------------
        // COUNTS
        // -------------------------------------------------------------------------

        @Transactional(readOnly = true)
        public long countUsersByProject(Long projectId) {

                Project project = getProject(projectId);

                projectAccessService.requireView(project);

                return projectUserRepository.countByProjectId(projectId);
        }

        @Transactional(readOnly = true)
        public long countProjectsByUser(Long userId) {

                validateUser(userId);

                return projectUserRepository.countByUserId(userId);
        }

        // -------------------------------------------------------------------------
        // VALIDATION
        // -------------------------------------------------------------------------

        private void validateRequest(
                        ProjectUserRequest request) {

                if (request == null) {
                        throw new IllegalArgumentException(
                                        "Project user request is required");
                }

                if (request.getProjectId() == null
                                || request.getProjectId() <= 0) {

                        throw new IllegalArgumentException(
                                        "Project ID must be positive");
                }

                if (request.getUserId() == null
                                || request.getUserId() <= 0) {

                        throw new IllegalArgumentException(
                                        "User ID must be positive");
                }

                if (request.getRole() == null) {
                        throw new IllegalArgumentException(
                                        "Project role is required");
                }
        }

        private String normalizeRole(String role) {
                if (role == null || role.trim().isEmpty()) {
                        throw new IllegalArgumentException("Project role is required");
                }

                String normalized = role.trim().toUpperCase();

                switch (normalized) {
                        case "MEMBER":
                                return ProjectRole.DEVELOPER.name();
                        case "TEAMLEAD":
                        case "TEAM_LEAD":
                                return ProjectRole.TEAM_LEAD.name();
                        case "PROJECTADMIN":
                        case "PROJECT_ADMIN":
                                return ProjectRole.PROJECT_ADMIN.name();
                        case "DEVELOPER":
                                return ProjectRole.DEVELOPER.name();
                        case "VIEWER":
                                return ProjectRole.VIEWER.name();
                        default:
                                try {
                                        return ProjectRole.valueOf(normalized).name();
                                } catch (IllegalArgumentException ex) {
                                        throw new IllegalArgumentException(
                                                        "Invalid project role: " + role);
                                }
                }
        }

        private String normalizeRequestedRole(ProjectUserRequest request) {
                ProjectRole requested = request.getRole();
                if (requested == null) {
                        throw new IllegalArgumentException("Project role is required");
                }

                // Backward compatibility: old MEMBER + responsibility values are
                // converted into the new first-class project roles.
                if (requested == ProjectRole.MEMBER) {
                        if (request.getResponsibilityRole() == MemberResponsibility.TEAM_LEAD) {
                                return ProjectRole.TEAM_LEAD.name();
                        }
                        return ProjectRole.DEVELOPER.name();
                }

                if (requested == ProjectRole.VIEWER) {
                        return ProjectRole.VIEWER.name();
                }

                return requested.name();
        }

        private void validateAdminCardinality(Long projectId, String role, Long excludedId) {
                if (!ProjectRole.PROJECT_ADMIN.name().equalsIgnoreCase(role)) {
                        return;
                }
                long count = projectUserRepository.countByProjectIdAndRole(
                                projectId, ProjectRole.PROJECT_ADMIN.name());
                if (excludedId != null) {
                        ProjectUser existing = projectUserRepository.findById(excludedId).orElse(null);
                        if (existing != null && projectId.equals(existing.getProject().getId())
                                        && ProjectRole.PROJECT_ADMIN.name().equalsIgnoreCase(existing.getRole())) {
                                count--;
                        }
                }
                if (count >= 2) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "A project can have a maximum of 2 Project Admins");
                }
        }

        private void ensureNotLastProjectAdmin(Long projectId, Long excludedId) {
                long count = projectUserRepository.countByProjectIdAndRole(
                                projectId, ProjectRole.PROJECT_ADMIN.name());
                if (excludedId != null) {
                        ProjectUser existing = projectUserRepository.findById(excludedId).orElse(null);
                        if (existing != null && ProjectRole.PROJECT_ADMIN.name().equalsIgnoreCase(existing.getRole())) {
                                count--;
                        }
                }
                if (count < 1) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "A project must always have at least 1 Project Admin. Assign another Project Admin first.");
                }
        }

        // -------------------------------------------------------------------------
        // HELPERS
        // -------------------------------------------------------------------------

        private ProjectUser getProjectUser(
                        Long projectUserId) {

                if (projectUserId == null
                                || projectUserId <= 0) {

                        throw new IllegalArgumentException(
                                        "Project user ID must be positive");
                }

                return projectUserRepository.findById(
                                projectUserId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project user not found with id: "
                                                                + projectUserId));
        }

        private Project getProject(Long projectId) {

                if (projectId == null || projectId <= 0) {

                        throw new IllegalArgumentException(
                                        "Project ID must be positive");
                }

                return projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project not found with id: "
                                                                + projectId));
        }

        private User getUser(Long userId) {

                return userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with id: " + userId));
        }

        private void validateUser(Long userId) {

                if (userId == null
                                || !userRepository.existsById(userId)) {

                        throw new RuntimeException(
                                        "User not found with id: " + userId);
                }
        }

        private ProjectUserResponse toResponse(
                        ProjectUser projectUser) {

                User user = projectUser.getUser();
                Project project = projectUser.getProject();

                return ProjectUserResponse.builder()
                                .id(projectUser.getId())
                                .userId(
                                                user != null
                                                                ? user.getId()
                                                                : null)
                                .userName(
                                                user != null
                                                                ? user.getName()
                                                                : null)
                                .userEmail(
                                                user != null
                                                                ? user.getEmail()
                                                                : null)
                                .projectId(
                                                project != null
                                                                ? project.getId()
                                                                : null)
                                .projectName(
                                                project != null
                                                                ? project.getName()
                                                                : null)
                                .role(projectUser.getRole())
                                .responsibilityRole(
                                                projectUser.getResponsibilityRole())
                                .availabilitySelfUpdateOpen(
                                                Boolean.TRUE.equals(
                                                                projectUser
                                                                                .getAvailabilitySelfUpdateOpen()))
                                .createdAt(
                                                projectUser.getCreatedAt())
                                .updatedAt(
                                                projectUser.getUpdatedAt())
                                .build();
        }

}