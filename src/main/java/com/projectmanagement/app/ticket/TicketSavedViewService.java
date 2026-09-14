package com.projectmanagement.app.ticket;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.*;

@Service @Transactional
public class TicketSavedViewService {
    private final TicketSavedViewRepository repository; private final ProjectRepository projectRepository;
    private final ProjectAccessService access; private final CurrentUserService currentUser;
    public TicketSavedViewService(TicketSavedViewRepository repository, ProjectRepository projectRepository, ProjectAccessService access, CurrentUserService currentUser) {
        this.repository = repository; this.projectRepository = projectRepository; this.access = access; this.currentUser = currentUser;
    }
    @Transactional(readOnly = true) public List<TicketSavedViewResponse> list(Long projectId) {
        Project project = project(projectId); access.requireView(project);
        return repository.findByProjectIdAndOwnerIdOrderByNameAsc(projectId, currentUser.getCurrentUserId()).stream().map(this::response).toList();
    }
    public TicketSavedViewResponse create(Long projectId, TicketSavedViewRequest request) {
        Project project = project(projectId); access.requireView(project); Long userId = currentUser.getCurrentUserId();
        String name = request.getName().trim(); if (repository.existsByProjectIdAndOwnerIdAndNameIgnoreCase(projectId, userId, name)) throw new RuntimeException("A saved view with this name already exists");
        return response(repository.save(build(TicketSavedView.builder().project(project).owner(currentUser.getCurrentUser()).name(name).build(), request)));
    }
    public TicketSavedViewResponse update(Long projectId, Long id, TicketSavedViewRequest request) {
        Long userId = currentUser.getCurrentUserId(); TicketSavedView view = repository.findByIdAndProjectIdAndOwnerId(id, projectId, userId).orElseThrow(() -> new RuntimeException("Saved view not found"));
        access.requireView(view.getProject()); String name = request.getName().trim();
        if (repository.existsByProjectIdAndOwnerIdAndNameIgnoreCaseAndIdNot(projectId, userId, name, id)) throw new RuntimeException("A saved view with this name already exists");
        view.setName(name); return response(repository.save(build(view, request)));
    }
    public void delete(Long projectId, Long id) {
        TicketSavedView view = repository.findByIdAndProjectIdAndOwnerId(id, projectId, currentUser.getCurrentUserId()).orElseThrow(() -> new RuntimeException("Saved view not found"));
        access.requireView(view.getProject()); repository.delete(view);
    }
    private Project project(Long id) { return projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found")); }
    private TicketSavedView build(TicketSavedView v, TicketSavedViewRequest r) { v.setQueryText(r.getQ()); v.setStatusId(r.getStatusId()); v.setPriorityId(r.getPriorityId()); v.setResponsibleId(r.getResponsibleId()); v.setSprintId(r.getSprintId()); v.setEpicId(r.getEpicId()); v.setLabelId(r.getLabelId()); v.setRootOnly(Boolean.TRUE.equals(r.getRootOnly())); return v; }
    private TicketSavedViewResponse response(TicketSavedView v) { return TicketSavedViewResponse.builder().id(v.getId()).projectId(v.getProject().getId()).name(v.getName()).q(v.getQueryText()).statusId(v.getStatusId()).priorityId(v.getPriorityId()).responsibleId(v.getResponsibleId()).sprintId(v.getSprintId()).epicId(v.getEpicId()).labelId(v.getLabelId()).rootOnly(v.getRootOnly()).createdAt(v.getCreatedAt()).updatedAt(v.getUpdatedAt()).build(); }
}
