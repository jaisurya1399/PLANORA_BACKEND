package com.projectmanagement.app.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectWorkingHoursService {

    public static final BigDecimal DEFAULT_WORKING_HOURS = BigDecimal.valueOf(8);

    private final ProjectWorkingHoursRepository repository;
    private final ProjectRepository projectRepository;
    private final ProjectAccessService projectAccessService;

    public ProjectWorkingHoursService(
            ProjectWorkingHoursRepository repository,
            ProjectRepository projectRepository,
            ProjectAccessService projectAccessService) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public List<ProjectWorkingHoursResponse> getHistory(Long projectId) {
        Project project = getProject(projectId);
        projectAccessService.requireView(project);
        return repository.findByProjectIdOrderByEffectiveFromAsc(projectId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getEffectiveHours(Long projectId, LocalDate date) {
        if (date == null)
            return DEFAULT_WORKING_HOURS;
        return repository
                .findByProjectIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(projectId, date)
                .stream().findFirst()
                .map(ProjectWorkingHours::getWorkingHours)
                .orElse(DEFAULT_WORKING_HOURS);
    }

    public ProjectWorkingHoursResponse upsert(Long projectId, ProjectWorkingHoursRequest request) {
        if (request == null || request.getEffectiveFrom() == null || request.getWorkingHours() == null) {
            throw new IllegalArgumentException("Effective date and working hours are required");
        }
        if (request.getWorkingHours().compareTo(BigDecimal.ZERO) < 0
                || request.getWorkingHours().compareTo(BigDecimal.valueOf(24)) > 0) {
            throw new IllegalArgumentException("Working hours must be between 0 and 24");
        }
        if (request.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Working hours can only be changed from today onward");
        }

        Project project = getProject(projectId);
        projectAccessService.requireManager(project);

        ProjectWorkingHours record = repository
                .findByProjectIdAndEffectiveFrom(projectId, request.getEffectiveFrom())
                .orElseGet(() -> ProjectWorkingHours.builder()
                        .project(project)
                        .effectiveFrom(request.getEffectiveFrom())
                        .build());

        record.setWorkingHours(request.getWorkingHours());
        return toResponse(repository.save(record));
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
    }

    private ProjectWorkingHoursResponse toResponse(ProjectWorkingHours value) {
        return ProjectWorkingHoursResponse.builder()
                .id(value.getId())
                .projectId(value.getProject().getId())
                .effectiveFrom(value.getEffectiveFrom())
                .workingHours(value.getWorkingHours())
                .createdAt(value.getCreatedAt())
                .updatedAt(value.getUpdatedAt())
                .build();
    }
}
