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

        if (project.getOwner() != null && userId.equals(project.getOwner().getId()))
            return true;
        return projectUserRepository.existsByProjectIdAndUserId(project.getId(), userId);
    }

    public void requireView(Project project) {
        if (!canView(project))
            throw new RuntimeException("You do not have access to this project");
    }

    public void requireEditor(Project project) {
        requireMinimumRole(project, ProjectRole.MEMBER);
        if (!isSystemAdmin() && project.getArchivedAt() != null) {
            throw new RuntimeException("Archived projects are read-only until unarchived");
        }
    }

    public void requireManager(Project project) {
        requireMinimumRole(project, ProjectRole.PROJECT_ADMIN);
    }

    public ProjectRole getCurrentProjectRole(Project project) {
        if (project == null)
            return null;
        if (isSystemAdmin())
            return ProjectRole.PROJECT_ADMIN;

        Long userId = currentUserService.getCurrentUserId();
        if (userId == null)
            return null;
        if (project.getOwner() != null && userId.equals(project.getOwner().getId()))
            return ProjectRole.PROJECT_ADMIN;

        return projectUserRepository.findByProjectIdAndUserId(project.getId(), userId)
                .map(member -> parseRole(member.getRole()))
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

        return "DEVELOPER".equalsIgnoreCase(getCurrentMemberResponsibility(project));
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
     * Board drag/drop rule: system/project admins can move any ticket; ordinary
     * project members can move only tickets where they are owner or assignee.
     */
    public void requireBoardTransitionAccess(Project project, Long ownerUserId, Long responsibleUserId) {
        requireEditor(project);
        if (getCurrentProjectRole(project) == ProjectRole.PROJECT_ADMIN)
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
        if (project == null || project.getArchivedAt() != null)
            return isSystemAdmin();
        ProjectRole role = getCurrentProjectRole(project);
        return role == ProjectRole.PROJECT_ADMIN || role == ProjectRole.MEMBER;
    }

    public boolean canManage(Project project) {
        return getCurrentProjectRole(project) == ProjectRole.PROJECT_ADMIN;
    }

    public void requireMinimumRole(Project project, ProjectRole minimumRole) {
        if (project == null)
            throw new RuntimeException("Project is required");
        if (isSystemAdmin())
            return;

        ProjectRole role = getCurrentProjectRole(project);
        boolean allowed = minimumRole == ProjectRole.MEMBER
                ? role == ProjectRole.MEMBER || role == ProjectRole.PROJECT_ADMIN
                : role == ProjectRole.PROJECT_ADMIN;

        if (!allowed) {
            throw new RuntimeException(minimumRole == ProjectRole.PROJECT_ADMIN
                    ? "Project admin access is required"
                    : "Project edit access is required");
        }
    }

    private ProjectRole parseRole(String value) {
        if (value == null)
            return null;
        try {
            return ProjectRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            // Backward compatibility for data created before V49.
            if ("ADMIN".equalsIgnoreCase(value) || "OWNER".equalsIgnoreCase(value))
                return ProjectRole.PROJECT_ADMIN;
            if ("VIEWER".equalsIgnoreCase(value))
                return ProjectRole.VIEWER;
            return ProjectRole.MEMBER;
        }
    }

    private boolean isSystemAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
