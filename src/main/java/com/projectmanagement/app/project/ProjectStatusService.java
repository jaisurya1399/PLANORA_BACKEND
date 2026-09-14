package com.projectmanagement.app.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProjectStatusService {

        private final ProjectStatusRepository projectStatusRepository;

        public ProjectStatusService(
                        ProjectStatusRepository projectStatusRepository) {
                this.projectStatusRepository = projectStatusRepository;
        }

        // ---------------------------------------------------------
        // GET ALL
        // ---------------------------------------------------------

        @Transactional(readOnly = true)
        public List<ProjectStatusResponse> getAllProjectStatuses() {

                return projectStatusRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ---------------------------------------------------------
        // GET ACTIVE
        // ---------------------------------------------------------

        @Transactional(readOnly = true)
        public List<ProjectStatusResponse> getActiveProjectStatuses() {

                return projectStatusRepository
                                .findByDeletedAtIsNull()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ---------------------------------------------------------
        // GET BY ID
        // ---------------------------------------------------------

        @Transactional(readOnly = true)
        public ProjectStatusResponse getProjectStatusById(
                        Long id) {

                ProjectStatus projectStatus = projectStatusRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project status not found with id: "
                                                                + id));

                return toResponse(projectStatus);
        }

        // ---------------------------------------------------------
        // GET BY NAME
        // ---------------------------------------------------------

        @Transactional(readOnly = true)
        public ProjectStatusResponse getProjectStatusByName(
                        String name) {

                ProjectStatus projectStatus = projectStatusRepository
                                .findByNameAndDeletedAtIsNull(name)
                                .orElseThrow(() -> new RuntimeException(
                                                "Active project status not found with name: "
                                                                + name));

                return toResponse(projectStatus);
        }

        // ---------------------------------------------------------
        // GET DEFAULT STATUSES
        // ---------------------------------------------------------

        @Transactional(readOnly = true)
        public List<ProjectStatusResponse> getDefaultProjectStatuses() {

                return projectStatusRepository
                                .findByIsDefaultTrue()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // ---------------------------------------------------------
        // CREATE
        // ---------------------------------------------------------

        public ProjectStatusResponse createProjectStatus(
                        ProjectStatusRequest request) {

                validateName(request.getName(), null);

                ProjectStatus projectStatus = ProjectStatus.builder()
                                .name(request.getName())
                                .color(
                                                request.getColor() == null ||
                                                                request.getColor().isBlank()
                                                                                ? "#cecece"
                                                                                : request.getColor())
                                .isDefault(
                                                request.getIsDefault() != null
                                                                ? request.getIsDefault()
                                                                : false)
                                .build();

                /*
                 * If this status is marked as default,
                 * make other statuses non-default.
                 */
                if (Boolean.TRUE.equals(projectStatus.getIsDefault())) {
                        clearExistingDefaultStatuses();
                }

                return toResponse(
                                projectStatusRepository.save(projectStatus));
        }

        // ---------------------------------------------------------
        // UPDATE
        // ---------------------------------------------------------

        public ProjectStatusResponse updateProjectStatus(
                        Long id,
                        ProjectStatusRequest request) {

                ProjectStatus projectStatus = projectStatusRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project status not found with id: "
                                                                + id));

                validateName(request.getName(), id);

                projectStatus.setName(request.getName());

                if (request.getColor() != null &&
                                !request.getColor().isBlank()) {

                        projectStatus.setColor(request.getColor());
                }

                if (request.getIsDefault() != null) {

                        projectStatus.setIsDefault(
                                        request.getIsDefault());

                        if (Boolean.TRUE.equals(
                                        request.getIsDefault())) {
                                clearExistingDefaultStatuses(id);
                        }
                }

                return toResponse(
                                projectStatusRepository.save(projectStatus));
        }

        // ---------------------------------------------------------
        // SOFT DELETE
        // ---------------------------------------------------------

        public void deleteProjectStatus(Long id) {

                ProjectStatus projectStatus = projectStatusRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project status not found with id: "
                                                                + id));

                projectStatus.setDeletedAt(
                                LocalDateTime.now());

                projectStatusRepository.save(projectStatus);
        }

        // ---------------------------------------------------------
        // RESTORE
        // ---------------------------------------------------------

        public ProjectStatusResponse restoreProjectStatus(
                        Long id) {

                ProjectStatus projectStatus = projectStatusRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Project status not found with id: "
                                                                + id));

                projectStatus.setDeletedAt(null);

                return toResponse(
                                projectStatusRepository.save(projectStatus));
        }

        // ---------------------------------------------------------
        // PERMANENT DELETE
        // ---------------------------------------------------------

        public void permanentlyDeleteProjectStatus(
                        Long id) {

                if (!projectStatusRepository.existsById(id)) {
                        throw new RuntimeException(
                                        "Project status not found with id: " + id);
                }

                projectStatusRepository.deleteById(id);
        }

        // ---------------------------------------------------------
        // VALIDATION
        // ---------------------------------------------------------

        private void validateName(
                        String name,
                        Long currentId) {

                boolean exists;

                if (currentId == null) {
                        exists = projectStatusRepository
                                        .existsByName(name);
                } else {
                        exists = projectStatusRepository
                                        .existsByNameAndIdNot(
                                                        name,
                                                        currentId);
                }

                if (exists) {
                        throw new RuntimeException(
                                        "Project status already exists with name: "
                                                        + name);
                }
        }

        // ---------------------------------------------------------
        // CLEAR DEFAULT
        // ---------------------------------------------------------

        private void clearExistingDefaultStatuses() {

                clearExistingDefaultStatuses(null);
        }

        private void clearExistingDefaultStatuses(
                        Long excludedId) {

                List<ProjectStatus> defaults = projectStatusRepository
                                .findByIsDefaultTrue();

                for (ProjectStatus status : defaults) {

                        if (excludedId == null ||
                                        !status.getId().equals(excludedId)) {

                                status.setIsDefault(false);

                                projectStatusRepository.save(status);
                        }
                }
        }

        // ---------------------------------------------------------
        // RESPONSE MAPPER
        // ---------------------------------------------------------

        private ProjectStatusResponse toResponse(
                        ProjectStatus projectStatus) {

                return ProjectStatusResponse.builder()
                                .id(projectStatus.getId())
                                .name(projectStatus.getName())
                                .color(projectStatus.getColor())
                                .isDefault(projectStatus.getIsDefault())
                                .deletedAt(projectStatus.getDeletedAt())
                                .createdAt(projectStatus.getCreatedAt())
                                .updatedAt(projectStatus.getUpdatedAt())
                                .build();
        }
}