package com.projectmanagement.app.dashboardanalytics;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;

@Service
@Transactional
public class CustomDashboardService {
    private final CustomDashboardRepository repository;
    private final CurrentUserService currentUserService;
    private final ProjectRepository projectRepository;
    private final ProjectAccessService accessService;
    private final ObjectMapper objectMapper;

    public CustomDashboardService(CustomDashboardRepository repository, CurrentUserService currentUserService,
            ProjectRepository projectRepository, ProjectAccessService accessService, ObjectMapper objectMapper) {
        this.repository = repository;
        this.currentUserService = currentUserService;
        this.projectRepository = projectRepository;
        this.accessService = accessService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<CustomDashboardResponse> getAll() {
        return repository.findByUserIdOrderByUpdatedAtDesc(currentUserService.getCurrentUserId()).stream()
                .map(this::toResponse).toList();
    }

    public CustomDashboardResponse create(CustomDashboardRequest request) {
        return save(null, request);
    }

    public CustomDashboardResponse update(Long id, CustomDashboardRequest request) {
        return save(id, request);
    }

    private CustomDashboardResponse save(Long id, CustomDashboardRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("Dashboard name is required");
        if (request.getProjectId() != null)
            requireProject(request.getProjectId());
        List<String> widgets = request.getWidgets() == null ? List.of("VELOCITY", "SPRINT_REPORT", "CUMULATIVE_FLOW",
                "CONTROL_CHART", "LEAD_CYCLE_TIME", "CREATED_RESOLVED") : request.getWidgets();
        Set<String> allowed = Set.of("VELOCITY", "SPRINT_REPORT", "CUMULATIVE_FLOW", "CONTROL_CHART", "LEAD_CYCLE_TIME",
                "CREATED_RESOLVED");
        if (widgets.stream().anyMatch(w -> w == null || !allowed.contains(w)))
            throw new IllegalArgumentException("Invalid dashboard widget");

        CustomDashboard dashboard;
        if (id == null)
            dashboard = CustomDashboard.builder().user(currentUserService.getCurrentUser()).build();
        else
            dashboard = repository.findById(id).orElseThrow(() -> new RuntimeException("Dashboard not found"));
        if (!dashboard.getUser().getId().equals(currentUserService.getCurrentUserId()))
            throw new RuntimeException("Dashboard access denied");
        try {
            dashboard.setWidgetsJson(objectMapper.writeValueAsString(widgets));
        } catch (Exception e) {
            throw new RuntimeException("Unable to save dashboard widgets", e);
        }
        dashboard.setName(request.getName().trim());
        dashboard.setProjectId(request.getProjectId());
        return toResponse(repository.save(dashboard));
    }

    public void delete(Long id) {
        CustomDashboard d = repository.findById(id).orElseThrow(() -> new RuntimeException("Dashboard not found"));
        if (!d.getUser().getId().equals(currentUserService.getCurrentUserId()))
            throw new RuntimeException("Dashboard access denied");
        repository.delete(d);
    }

    private void requireProject(Long id) {
        Project p = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        accessService.requireView(p);
    }

    private CustomDashboardResponse toResponse(CustomDashboard d) {
        try {
            List<String> widgets = objectMapper.readValue(d.getWidgetsJson(), new TypeReference<List<String>>() {
            });
            return CustomDashboardResponse.builder().id(d.getId()).name(d.getName()).projectId(d.getProjectId())
                    .widgets(widgets).createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
        } catch (Exception e) {
            throw new RuntimeException("Invalid dashboard configuration", e);
        }
    }
}
