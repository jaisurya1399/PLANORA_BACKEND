package com.projectmanagement.app.project;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
@Transactional
public class ProjectTemplateService {
    private final ProjectTemplateRepository repository;
    private final ProjectRepository projectRepository;
    private final ProjectStatusRepository statusRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectAccessService access;
    private final CurrentUserService currentUser;

    public ProjectTemplateService(ProjectTemplateRepository repository, ProjectRepository projectRepository,
            ProjectStatusRepository statusRepository, UserRepository userRepository,
            ProjectUserRepository projectUserRepository, ProjectAccessService access, CurrentUserService currentUser) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
        this.projectUserRepository = projectUserRepository;
        this.access = access;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<ProjectTemplateResponse> list() {
        if (isAdmin())
            return repository.findAllByOrderByUpdatedAtDesc().stream().map(this::response).toList();
        return repository.findByCreatedByIdOrderByUpdatedAtDesc(currentUser.getCurrentUserId()).stream()
                .map(this::response).toList();
    }

    public ProjectTemplateResponse create(ProjectTemplateRequest request) {
        User creator = currentUser.getCurrentUser();
        if (request.getSourceProjectId() == null)
            throw new IllegalArgumentException("Source project is required");
        Project source = projectRepository.findById(request.getSourceProjectId())
                .orElseThrow(() -> new RuntimeException("Source project not found"));
        access.requireManager(source);
        if (source.getDeletedAt() != null || source.getArchivedAt() != null)
            throw new RuntimeException("Archived or deleted projects cannot be saved as templates");
        String name = request.getName().trim();
        if (repository.existsByNameIgnoreCaseAndCreatedById(name, creator.getId()))
            throw new RuntimeException("A template with this name already exists");
        ProjectTemplate template = ProjectTemplate.builder()
                .name(name)
                .description(request.getDescription() == null ? source.getDescription() : request.getDescription())
                .ticketPrefix(source.getTicketPrefix())
                .status(source.getStatus())
                .statusType(source.getStatusType())
                .createdBy(creator)
                .build();
        return response(repository.save(template));
    }

    public ProjectResponse createProject(Long templateId, ProjectTemplateCreateProjectRequest request) {
        ProjectTemplate template = getAccessibleTemplate(templateId);
        String name = request.getName().trim();
        String prefix = request.getTicketPrefix().trim();
        if (projectRepository.existsByName(name))
            throw new RuntimeException("Project already exists with name: " + name);
        if (projectRepository.existsByTicketPrefix(prefix))
            throw new RuntimeException("Ticket prefix already exists: " + prefix);
        User owner = resolveOwner(request.getOwnerId());
        Project project = Project.builder().name(name).description(template.getDescription()).owner(owner)
                .status(template.getStatus()).ticketPrefix(prefix).statusType(template.getStatusType()).build();
        Project saved = projectRepository.save(project);
        if (!projectUserRepository.existsByProjectIdAndUserId(saved.getId(), owner.getId())) {
            projectUserRepository.save(
                    ProjectUser.builder().project(saved).user(owner).role(ProjectRole.PROJECT_ADMIN.name()).build());
        }
        return toProjectResponse(saved);
    }

    public void delete(Long id) {
        ProjectTemplate template = getAccessibleTemplate(id);
        repository.delete(template);
    }

    private ProjectTemplate getAccessibleTemplate(Long id) {
        ProjectTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project template not found"));
        if (!isAdmin() && !template.getCreatedBy().getId().equals(currentUser.getCurrentUserId()))
            throw new RuntimeException("You do not have access to this project template");
        return template;
    }

    private User resolveOwner(Long requestedOwnerId) {
        if (!isAdmin())
            return currentUser.getCurrentUser();
        if (requestedOwnerId == null)
            return currentUser.getCurrentUser();
        return userRepository.findById(requestedOwnerId).orElseThrow(() -> new RuntimeException("Owner not found"));
    }

    private boolean isAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(x -> "ROLE_ADMIN".equals(x.getAuthority()));
    }

    private ProjectTemplateResponse response(ProjectTemplate t) {
        return ProjectTemplateResponse.builder().id(t.getId()).name(t.getName()).description(t.getDescription())
                .ticketPrefix(t.getTicketPrefix()).statusId(t.getStatus() == null ? null : t.getStatus().getId())
                .statusName(t.getStatus() == null ? null : t.getStatus().getName())
                .statusColor(t.getStatus() == null ? null : t.getStatus().getColor()).statusType(t.getStatusType())
                .createdById(t.getCreatedBy().getId()).createdByName(t.getCreatedBy().getName())
                .createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt()).build();
    }

    private ProjectResponse toProjectResponse(Project p) {
        return ProjectResponse.builder().id(p.getId()).name(p.getName()).description(p.getDescription())
                .ownerId(p.getOwner().getId()).ownerName(p.getOwner().getName()).ownerEmail(p.getOwner().getEmail())
                .statusId(p.getStatus().getId()).statusName(p.getStatus().getName())
                .statusColor(p.getStatus().getColor())
                .ticketPrefix(p.getTicketPrefix()).statusType(p.getStatusType()).deletedAt(p.getDeletedAt())
                .archivedAt(p.getArchivedAt()).createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
    }
}
