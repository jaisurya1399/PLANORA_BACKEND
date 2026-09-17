package com.projectmanagement.app.project;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;

@Service
@Transactional(readOnly = true)
public class ProjectAccessService {

    private final CurrentUserService currentUserService;
    private final ProjectUserRepository projectUserRepository;

    public ProjectAccessService(CurrentUserService currentUserService,
            ProjectUserRepository projectUserRepository) {
        this.currentUserService = currentUserService;
        this.projectUserRepository = projectUserRepository;
    }

    public boolean canView(Project project) {
        if (project == null)
            return false;
        if (isSystemAdmin())
            return true;

        Long userId = currentUserService.getCurrentUserId();
        if (userId == null)
            return false;

        return projectUserRepository.existsByProjectIdAndUserId(project.getId(), userId);
    }

    public void requireView(Project project) {
        if (!canView(project))
            throw new RuntimeException("You do not have access to this project");
    }

    /**
     * Strict project-scope check for operational resources. System Admin can
     * inspect project metadata but is not a project member and therefore cannot
     * operate on project-scoped work resources through this path.
     */
    public void requireProjectMember(Project project) {
        if (project == null) {
            throw new org.springframework.security.access.AccessDeniedException("Project is required");
        }
        Long userId = currentUserService.getCurrentUserId();
        if (userId == null || !projectUserRepository.existsByProjectIdAndUserId(project.getId(), userId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Project membership is required for this operation");
        }
    }

    public void requireEditor(Project project) {
        requireMinimumRole(project, ProjectRole.DEVELOPER);
        if (project.getArchivedAt() != null) {
            throw new RuntimeException("Archived projects are read-only until unarchived");
        }
    }

    public void requireManager(Project project) {
        requireMinimumRole(project, ProjectRole.PROJECT_ADMIN);
    }

    /**
     * Project membership administration may be performed by System Admin or the
     * Project Admin of the project.
     */
    public void requireProjectMembershipAdministration(Project project) {
        if (isSystemAdmin())
            return;
        requireManager(project);
    }

    /** Project Admin or System Admin. */
    public void requireProjectAdmin(Project project) {
        requireManager(project);
    }

    /** Project Admin, Team Lead, or System Admin. */
    public void requireTeamLeadOrAdmin(Project project) {
        ProjectRole role = getCurrentProjectRole(project);
        if (role != ProjectRole.PROJECT_ADMIN && role != ProjectRole.TEAM_LEAD) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Project Admin or Team Lead access is required");
        }
    }

    /** Ticket assignment is not a Developer/Viewer capability. */
    public void requireTicketAssignment(Project project) {
        requireTeamLeadOrAdmin(project);
    }

    /**
     * Status changes are allowed to project editors. Developers must be involved.
     */
    public void requireTicketStatusChange(Project project, Long ownerUserId, Long responsibleUserId) {
        requireEditor(project);
        ProjectRole role = getCurrentProjectRole(project);
        if (role == ProjectRole.PROJECT_ADMIN || role == ProjectRole.TEAM_LEAD)
            return;
        Long currentUserId = currentUserService.getCurrentUserId();
        boolean involved = currentUserId != null &&
                (currentUserId.equals(ownerUserId) || currentUserId.equals(responsibleUserId));
        if (!involved) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Developers can change status only for tickets where they are involved");
        }
    }

    /** Backlog planning is a Project Admin / Team Lead capability. */
    public void requireBacklogManage(Project project) {
        requireTeamLeadOrAdmin(project);
    }

    /** Sprint planning/administration is a Project Admin / Team Lead capability. */
    public void requireSprintManage(Project project) {
        requireTeamLeadOrAdmin(project);
    }

    /** Project report access includes read-only project members. */
    public void requireReportView(Project project) {
        requireView(project);
    }

    /** Audit is a Project Admin / System Admin capability. */
    public void requireAuditView(Project project) {
        requireManager(project);
    }

    /** Workflow/board/master-data/security configuration is Project Admin only. */
    public void requireProjectConfiguration(Project project) {
        requireManager(project);
    }

    /**
     * Project administration entry point used for platform-level project metadata.
     */
    public void requireProjectAdministration(Project project) {
        // System Admin manages the project lifecycle itself (create/update/archive),
        // while all operational project administration remains Project Admin-only.
        if (isSystemAdmin())
            return;
        requireManager(project);
    }

    public void requireSystemAdmin() {
        if (!isSystemAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "System Admin access is required");
        }
    }

    public ProjectRole getCurrentProjectRole(Project project) {
        if (project == null)
            return null;
        Long userId = currentUserService.getCurrentUserId();
        if (userId == null)
            return null;
        return projectUserRepository.findByProjectIdAndUserId(project.getId(), userId)
                .map(member -> parseRole(member.getRole(), member.getResponsibilityRole()))
                .orElse(null);
    }

    public String getCurrentMemberResponsibility(Project project) {
        if (project == null || isSystemAdmin())
            return null;
        Long userId = currentUserService.getCurrentUserId();
        if (userId == null)
            return null;
        return projectUserRepository.findByProjectIdAndUserId(project.getId(), userId)
                .filter(member -> ProjectRole.MEMBER.name().equalsIgnoreCase(member.getRole()))
                .map(ProjectUser::getResponsibilityRole)
                .orElse(null);
    }

    /**
     * Returns true when the current user is a project MEMBER whose responsibility
     * is Developer. Developer visibility remains project-wide, but ticket edits
     * are restricted to tickets assigned to the current user.
     */
    public boolean isDeveloper(Project project) {
        if (project == null || isSystemAdmin())
            return false;

        ProjectRole role = getCurrentProjectRole(project);
        return role == ProjectRole.DEVELOPER ||
                (role == ProjectRole.MEMBER && "DEVELOPER".equalsIgnoreCase(getCurrentMemberResponsibility(project)));
    }

    /**
     * Developer-specific ticket edit rule. The caller must already have the
     * normal ticket.update authority; this method adds the ownership constraint.
     */
    public void requireDeveloperAssignment(Project project, Long responsibleUserId) {
        if (!isDeveloper(project))
            return;

        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId == null || responsibleUserId == null
                || !currentUserId.equals(responsibleUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Developers can edit only tickets assigned to themselves");
        }
    }

    /**
     * Board drag/drop rule: Project Admins and Team Leads can move any ticket;
     * Developers can move only tickets where they are owner or assignee.
     */
    public void requireBoardTransitionAccess(Project project, Long ownerUserId, Long responsibleUserId) {
        requireEditor(project);
        ProjectRole role = getCurrentProjectRole(project);
        if (role == ProjectRole.PROJECT_ADMIN || role == ProjectRole.TEAM_LEAD)
            return;

        Long currentUserId = currentUserService.getCurrentUserId();
        boolean involved = currentUserId != null
                && (currentUserId.equals(ownerUserId) || currentUserId.equals(responsibleUserId));
        if (!involved) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can change status only for tickets where you are involved");
        }
    }

    /**
     * Developer users are not allowed to perform project-wide board planning
     * operations. Project Admins and other permitted project editors retain the
     * existing behavior.
     */
    public void requireBoardPlanningAccess(Project project) {
        requireEditor(project);
        if (isDeveloper(project)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Developers cannot perform project-wide board planning");
        }
    }

    public boolean canEdit(Project project) {
        if (project == null)
            return false;
        if (project.getArchivedAt() != null)
            return false;
        ProjectRole role = getCurrentProjectRole(project);
        return role == ProjectRole.PROJECT_ADMIN || role == ProjectRole.TEAM_LEAD ||
                role == ProjectRole.DEVELOPER || role == ProjectRole.MEMBER;
    }

    public boolean canManage(Project project) {
        return getCurrentProjectRole(project) == ProjectRole.PROJECT_ADMIN;
    }

    public void requireMinimumRole(Project project, ProjectRole minimumRole) {
        if (project == null)
            throw new RuntimeException("Project is required");
        ProjectRole role = getCurrentProjectRole(project);
        boolean allowed = minimumRole == ProjectRole.PROJECT_ADMIN
                ? role == ProjectRole.PROJECT_ADMIN
                : role == ProjectRole.PROJECT_ADMIN || role == ProjectRole.TEAM_LEAD
                        || role == ProjectRole.DEVELOPER || role == ProjectRole.MEMBER;

        if (!allowed) {
            throw new org.springframework.security.access.AccessDeniedException(
                    minimumRole == ProjectRole.PROJECT_ADMIN
                            ? "Project admin access is required"
                            : "Project edit access is required");
        }
    }

    /**
     * Returns the effective project role and permissions for the authenticated
     * user.
     */
    public ProjectAccessResponse getCurrentAccess(Project project) {
        requireView(project);
        if (isSystemAdmin()) {
            return ProjectAccessResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .role("SYSTEM_ADMIN")
                    .permissions(java.util.List.of(
                            "system.manage",
                            "user.manage",
                            "role.manage",
                            "permission.manage",
                            "project.create",
                            "project.view",
                            "security.manage"))
                    .build();
        }
        ProjectRole role = getCurrentProjectRole(project);
        java.util.List<String> permissions = ProjectPermissionCatalog.forRole(role != null ? role.name() : null);

        return ProjectAccessResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .role(role != null ? role.name() : null)
                .permissions(permissions)
                .build();
    }

    private ProjectRole parseRole(String value, String responsibilityRole) {
        if (value == null)
            return null;
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if ("ADMIN".equals(normalized) || "OWNER".equals(normalized) || "PROJECTADMIN".equals(normalized))
            return ProjectRole.PROJECT_ADMIN;
        if ("TEAMLEAD".equals(normalized))
            return ProjectRole.TEAM_LEAD;
        if ("MEMBER".equals(normalized)) {
            if ("TEAM_LEAD".equalsIgnoreCase(responsibilityRole) || "TEAMLEAD".equalsIgnoreCase(responsibilityRole))
                return ProjectRole.TEAM_LEAD;
            return ProjectRole.DEVELOPER;
        }
        try {
            return ProjectRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return ProjectRole.DEVELOPER;
        }
    }

    private boolean isSystemAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
