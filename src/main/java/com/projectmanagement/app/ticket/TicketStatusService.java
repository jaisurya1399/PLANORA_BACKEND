package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;

@Service
@Transactional
public class TicketStatusService {

        private final TicketStatusRepository ticketStatusRepository;
        private final ProjectRepository projectRepository;

        public TicketStatusService(
                        TicketStatusRepository ticketStatusRepository,
                        ProjectRepository projectRepository) {

                this.ticketStatusRepository = ticketStatusRepository;
                this.projectRepository = projectRepository;
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getAll() {

                return ticketStatusRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ACTIVE
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getAllActive() {

                return ticketStatusRepository
                                .findByDeletedAtIsNull()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @Transactional(readOnly = true)
        public TicketStatusResponse getById(Long id) {

                TicketStatus status = ticketStatusRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with id: " + id));

                return toResponse(status);
        }

        // =========================================================
        // GET BY PROJECT
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getByProject(
                        Long projectId) {

                return ticketStatusRepository
                                .findByProjectIdOrderByOrderAsc(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ACTIVE BY PROJECT
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getActiveByProject(
                        Long projectId) {

                return ticketStatusRepository
                                .findByProjectIdAndDeletedAtIsNullOrderByOrderAsc(
                                                projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET GLOBAL STATUSES
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getGlobalStatuses() {

                return ticketStatusRepository
                                .findByProjectIdIsNull()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ACTIVE GLOBAL STATUSES
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getActiveGlobalStatuses() {

                return ticketStatusRepository
                                .findByProjectIdIsNullAndDeletedAtIsNull()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET DEFAULT STATUSES
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getDefaultStatuses() {

                return ticketStatusRepository
                                .findByIsDefaultTrue()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET DEFAULT STATUSES BY PROJECT
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getDefaultStatusesByProject(
                        Long projectId) {

                return ticketStatusRepository
                                .findByProjectIdAndIsDefaultTrue(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY NAME
        // =========================================================

        @Transactional(readOnly = true)
        public TicketStatusResponse getByName(
                        String name) {

                TicketStatus status = ticketStatusRepository
                                .findByName(name)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with name: "
                                                                + name));

                return toResponse(status);
        }

        // =========================================================
        // GET BY PROJECT + NAME
        // =========================================================

        @Transactional(readOnly = true)
        public TicketStatusResponse getByProjectAndName(
                        Long projectId,
                        String name) {

                TicketStatus status = ticketStatusRepository
                                .findByProjectIdAndName(
                                                projectId,
                                                name)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found"));

                return toResponse(status);
        }

        // =========================================================
        // GET BY COLOR
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketStatusResponse> getByColor(
                        String color) {

                return ticketStatusRepository
                                .findByColor(color)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // CREATE
        // =========================================================

        public TicketStatusResponse create(
                        TicketStatusRequest request) {

                Long projectId = request.getProjectId();

                Project project = null;

                if (projectId != null) {

                        project = projectRepository
                                        .findById(projectId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Project not found with id: "
                                                                        + projectId));
                }

                validateNameForCreate(
                                projectId,
                                request.getName());

                TicketStatus status = TicketStatus.builder()
                                .name(request.getName().trim())
                                .color(
                                                request.getColor() == null ||
                                                                request.getColor().isBlank()
                                                                                ? "#cecece"
                                                                                : request.getColor().trim())
                                .isDefault(
                                                request.getIsDefault() != null
                                                                ? request.getIsDefault()
                                                                : false)
                                .order(
                                                request.getOrder() != null
                                                                ? request.getOrder()
                                                                : 1)
                                .category(request.getCategory())
                                .project(project)
                                .build();

                if (Boolean.TRUE.equals(status.getIsDefault())) {

                        clearDefaultStatuses(projectId);
                }

                TicketStatus saved = ticketStatusRepository.save(status);

                return toResponse(saved);
        }

        // =========================================================
        // UPDATE
        // =========================================================

        public TicketStatusResponse update(
                        Long id,
                        TicketStatusRequest request) {

                TicketStatus status = ticketStatusRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with id: "
                                                                + id));

                Long projectId = request.getProjectId();

                Project project = null;

                if (projectId != null) {

                        project = projectRepository
                                        .findById(projectId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Project not found with id: "
                                                                        + projectId));
                }

                validateNameForUpdate(
                                projectId,
                                request.getName(),
                                id);

                status.setName(request.getName().trim());

                if (request.getColor() != null &&
                                !request.getColor().isBlank()) {

                        status.setColor(
                                        request.getColor().trim());
                }

                status.setOrder(
                                request.getOrder() != null
                                                ? request.getOrder()
                                                : 1);
                status.setCategory(request.getCategory());

                status.setProject(project);

                boolean makeDefault = Boolean.TRUE.equals(
                                request.getIsDefault());

                if (makeDefault) {

                        clearDefaultStatuses(
                                        projectId,
                                        id);

                        status.setIsDefault(true);

                } else {

                        status.setIsDefault(false);
                }

                TicketStatus updated = ticketStatusRepository.save(status);

                return toResponse(updated);
        }

        // =========================================================
        // SOFT DELETE
        // =========================================================

        public void delete(Long id) {

                TicketStatus status = ticketStatusRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with id: "
                                                                + id));

                status.setDeletedAt(
                                java.time.LocalDateTime.now());

                status.setIsDefault(false);

                ticketStatusRepository.save(status);
        }

        // =========================================================
        // RESTORE
        // =========================================================

        public TicketStatusResponse restore(Long id) {

                TicketStatus status = ticketStatusRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket status not found with id: "
                                                                + id));

                status.setDeletedAt(null);

                return toResponse(
                                ticketStatusRepository.save(status));
        }

        // =========================================================
        // PERMANENT DELETE
        // =========================================================

        public void permanentDelete(Long id) {

                if (!ticketStatusRepository.existsById(id)) {

                        throw new RuntimeException(
                                        "Ticket status not found with id: " + id);
                }

                ticketStatusRepository.deleteById(id);
        }

        // =========================================================
        // VALIDATE NAME - CREATE
        // =========================================================

        private void validateNameForCreate(
                        Long projectId,
                        String name) {

                if (name == null || name.isBlank()) {

                        throw new RuntimeException(
                                        "Ticket status name is required");
                }

                String trimmedName = name.trim();

                if (projectId == null) {

                        if (ticketStatusRepository
                                        .existsByProjectIdAndName(
                                                        null,
                                                        trimmedName)) {

                                throw new RuntimeException(
                                                "Global ticket status already exists: "
                                                                + trimmedName);
                        }

                } else {

                        if (ticketStatusRepository
                                        .existsByProjectIdAndName(
                                                        projectId,
                                                        trimmedName)) {

                                throw new RuntimeException(
                                                "Ticket status already exists in project: "
                                                                + trimmedName);
                        }
                }
        }

        // =========================================================
        // VALIDATE NAME - UPDATE
        // =========================================================

        private void validateNameForUpdate(
                        Long projectId,
                        String name,
                        Long id) {

                if (name == null || name.isBlank()) {

                        throw new RuntimeException(
                                        "Ticket status name is required");
                }

                String trimmedName = name.trim();

                if (ticketStatusRepository
                                .existsByProjectIdAndNameAndIdNot(
                                                projectId,
                                                trimmedName,
                                                id)) {

                        throw new RuntimeException(
                                        "Ticket status already exists: "
                                                        + trimmedName);
                }
        }

        // =========================================================
        // CLEAR DEFAULT STATUSES
        // =========================================================

        private void clearDefaultStatuses(
                        Long projectId) {

                List<TicketStatus> defaults;

                if (projectId == null) {

                        defaults = ticketStatusRepository
                                        .findByProjectIdIsNull()
                                        .stream()
                                        .filter(
                                                        status -> Boolean.TRUE.equals(
                                                                        status.getIsDefault()))
                                        .toList();

                } else {

                        defaults = ticketStatusRepository
                                        .findByProjectIdAndIsDefaultTrue(
                                                        projectId);
                }

                for (TicketStatus status : defaults) {

                        status.setIsDefault(false);
                }

                ticketStatusRepository.saveAll(defaults);
        }

        // =========================================================
        // CLEAR DEFAULT EXCEPT CURRENT ID
        // =========================================================

        private void clearDefaultStatuses(
                        Long projectId,
                        Long exceptId) {

                List<TicketStatus> defaults;

                if (projectId == null) {

                        defaults = ticketStatusRepository
                                        .findByProjectIdIsNull()
                                        .stream()
                                        .filter(
                                                        status -> Boolean.TRUE.equals(
                                                                        status.getIsDefault()))
                                        .toList();

                } else {

                        defaults = ticketStatusRepository
                                        .findByProjectIdAndIsDefaultTrue(
                                                        projectId);
                }

                for (TicketStatus status : defaults) {

                        if (!status.getId().equals(exceptId)) {

                                status.setIsDefault(false);
                        }
                }

                ticketStatusRepository.saveAll(defaults);
        }

        // =========================================================
        // ENTITY -> RESPONSE
        // =========================================================

        private TicketStatusResponse toResponse(
                        TicketStatus status) {

                Long projectId = null;
                String projectName = null;

                if (status.getProject() != null) {

                        projectId = status.getProject().getId();

                        projectName = status.getProject().getName();
                }

                return TicketStatusResponse.builder()
                                .id(status.getId())
                                .name(status.getName())
                                .color(status.getColor())
                                .isDefault(status.getIsDefault())
                                .order(status.getOrder())
                                .category(status.getCategory())
                                .projectId(projectId)
                                .projectName(projectName)
                                .deletedAt(status.getDeletedAt())
                                .createdAt(status.getCreatedAt())
                                .updatedAt(status.getUpdatedAt())
                                .build();
        }
}
