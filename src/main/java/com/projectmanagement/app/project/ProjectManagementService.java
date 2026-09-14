package com.projectmanagement.app.project;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.audit.AuditService;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
@Transactional
public class ProjectManagementService {
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectWorkingHoursRepository workingHoursRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService access;
    private final CurrentUserService currentUser;
    private final AuditService audit;

    public ProjectManagementService(ProjectRepository projectRepository, ProjectUserRepository projectUserRepository,
            ProjectWorkingHoursRepository workingHoursRepository, UserRepository userRepository,
            ProjectAccessService access, CurrentUserService currentUser, AuditService audit) {
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.userRepository = userRepository;
        this.access = access;
        this.currentUser = currentUser;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public ProjectResponse settings(Long id) {
        Project project = get(id);
        access.requireManager(project);
        return response(project);
    }

    public ProjectResponse archive(Long id) {
        Project project = get(id);
        access.requireManager(project);
        if (project.getDeletedAt() != null)
            throw new RuntimeException("Deleted projects cannot be archived");
        if (project.getArchivedAt() == null) {
            project.setArchivedAt(java.time.LocalDateTime.now());
            projectRepository.save(project);
            audit.record(project, null, "PROJECT_ARCHIVED", "PROJECT", project.getId(), Map.of());
        }
        return response(project);
    }

    public ProjectResponse unarchive(Long id) {
        Project project = get(id);
        access.requireManager(project);
        project.setArchivedAt(null);
        projectRepository.save(project);
        audit.record(project, null, "PROJECT_UNARCHIVED", "PROJECT", project.getId(), Map.of());
        return response(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> archived() {
        return projectRepository.findByArchivedAtIsNotNullAndDeletedAtIsNull().stream()
                .filter(access::canView).map(this::response).toList();
    }

    public ProjectResponse cloneProject(Long sourceId, ProjectCloneRequest request) {
        Project source = get(sourceId);
        access.requireManager(source);
        if (source.getDeletedAt() != null || source.getArchivedAt() != null)
            throw new RuntimeException("Archived or deleted projects cannot be cloned");
        String name = request.getName().trim();
        String prefix = request.getTicketPrefix().trim();
        if (projectRepository.existsByName(name))
            throw new RuntimeException("Project already exists with name: " + name);
        if (projectRepository.existsByTicketPrefix(prefix))
            throw new RuntimeException("Ticket prefix already exists: " + prefix);

        User owner = resolveOwner(request.getOwnerId());
        Project clone = Project.builder().name(name).description(source.getDescription()).owner(owner)
                .status(source.getStatus()).ticketPrefix(prefix).statusType(source.getStatusType()).build();
        Project saved = projectRepository.save(clone);

        // Clone membership only when explicitly requested. This never copies tickets,
        // comments,
        // attachments, audit history, favorites, or other user-generated work.
        if (Boolean.TRUE.equals(request.getCopyMembers())) {
            for (ProjectUser member : projectUserRepository.findByProjectId(sourceId)) {
                if (projectUserRepository.existsByProjectIdAndUserId(saved.getId(), member.getUser().getId()))
                    continue;
                projectUserRepository.save(ProjectUser.builder().project(saved).user(member.getUser())
                        .role(member.getRole()).responsibilityRole(member.getResponsibilityRole())
                        .availabilitySelfUpdateOpen(Boolean.FALSE).build());
            }
        }

        // Copy working-hours history because it is project configuration, not
        // user-generated work.
        workingHoursRepository.findByProjectIdOrderByEffectiveFromAsc(sourceId)
                .forEach(item -> workingHoursRepository.save(ProjectWorkingHours.builder().project(saved)
                        .effectiveFrom(item.getEffectiveFrom()).workingHours(item.getWorkingHours()).build()));

        if (!projectUserRepository.existsByProjectIdAndUserId(saved.getId(), owner.getId())) {
            projectUserRepository.save(ProjectUser.builder().project(saved).user(owner)
                    .role(ProjectRole.PROJECT_ADMIN.name()).build());
        }

        audit.record(saved, null, "PROJECT_CLONED", "PROJECT", saved.getId(),
                Map.of("sourceProjectId", source.getId(), "copiedMembers",
                        Boolean.TRUE.equals(request.getCopyMembers())));
        return response(saved);
    }

    private User resolveOwner(Long ownerId) {
        if (!isAdmin())
            return currentUser.getCurrentUser();
        if (ownerId == null)
            return currentUser.getCurrentUser();
        return userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Owner not found"));
    }

    private Project get(Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Project ID must be positive");
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    private boolean isAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(x -> "ROLE_ADMIN".equals(x.getAuthority()));
    }

    private ProjectResponse response(Project p) {
        return ProjectResponse.builder().id(p.getId()).name(p.getName()).description(p.getDescription())
                .ownerId(p.getOwner().getId()).ownerName(p.getOwner().getName()).ownerEmail(p.getOwner().getEmail())
                .statusId(p.getStatus().getId()).statusName(p.getStatus().getName())
                .statusColor(p.getStatus().getColor())
                .ticketPrefix(p.getTicketPrefix()).statusType(p.getStatusType()).deletedAt(p.getDeletedAt())
                .archivedAt(p.getArchivedAt()).createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
    }
}
