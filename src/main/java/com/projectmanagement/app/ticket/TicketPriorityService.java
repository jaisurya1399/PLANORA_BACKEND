package com.projectmanagement.app.ticket;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TicketPriorityService {

        private final TicketPriorityRepository ticketPriorityRepository;

        public TicketPriorityService(
                        TicketPriorityRepository ticketPriorityRepository) {

                this.ticketPriorityRepository = ticketPriorityRepository;
        }

        // =========================================================
        // GET ALL
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketPriorityResponse> getAll() {

                return ticketPriorityRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET ACTIVE
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketPriorityResponse> getAllActive() {

                return ticketPriorityRepository.findByDeletedAtIsNull()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @Transactional(readOnly = true)
        public TicketPriorityResponse getById(Long id) {

                TicketPriority priority = ticketPriorityRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket priority not found with id: " + id));

                return toResponse(priority);
        }

        // =========================================================
        // GET BY NAME
        // =========================================================

        @Transactional(readOnly = true)
        public TicketPriorityResponse getByName(String name) {

                TicketPriority priority = ticketPriorityRepository
                                .findByName(name)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket priority not found with name: " + name));

                return toResponse(priority);
        }

        // =========================================================
        // GET DEFAULT PRIORITIES
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketPriorityResponse> getDefaultPriorities() {

                return ticketPriorityRepository.findByIsDefaultTrue()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // GET BY COLOR
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketPriorityResponse> getByColor(String color) {

                return ticketPriorityRepository.findByColor(color)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // =========================================================
        // CREATE
        // =========================================================

        public TicketPriorityResponse create(
                        TicketPriorityRequest request) {

                validateNameForCreate(request.getName());

                TicketPriority priority = TicketPriority.builder()
                                .name(request.getName().trim())
                                .displayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder())
                                .color(
                                                request.getColor() == null ||
                                                                request.getColor().isBlank()
                                                                                ? "#cecece"
                                                                                : request.getColor().trim())
                                .isDefault(
                                                request.getIsDefault() != null
                                                                ? request.getIsDefault()
                                                                : false)
                                .build();

                /*
                 * Only one default priority is maintained by the
                 * application business logic.
                 */
                if (Boolean.TRUE.equals(priority.getIsDefault())) {
                        clearDefaultPriorities();
                }

                TicketPriority saved = ticketPriorityRepository.save(priority);

                return toResponse(saved);
        }

        // =========================================================
        // UPDATE
        // =========================================================

        public TicketPriorityResponse update(
                        Long id,
                        TicketPriorityRequest request) {

                TicketPriority priority = ticketPriorityRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket priority not found with id: " + id));

                validateNameForUpdate(
                                request.getName(),
                                id);

                priority.setName(request.getName().trim());
                if (request.getDisplayOrder() != null)
                        priority.setDisplayOrder(request.getDisplayOrder());

                if (request.getColor() != null &&
                                !request.getColor().isBlank()) {

                        priority.setColor(request.getColor().trim());
                }

                boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault());

                if (makeDefault) {
                        clearDefaultPriorities(id);
                        priority.setIsDefault(true);
                } else {
                        priority.setIsDefault(false);
                }

                TicketPriority updated = ticketPriorityRepository.save(priority);

                return toResponse(updated);
        }

        // =========================================================
        // SOFT DELETE
        // =========================================================

        public void delete(Long id) {

                TicketPriority priority = ticketPriorityRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket priority not found with id: " + id));

                priority.setDeletedAt(LocalDateTime.now());

                /*
                 * A deleted priority should not remain the default.
                 */
                priority.setIsDefault(false);

                ticketPriorityRepository.save(priority);
        }

        // =========================================================
        // RESTORE
        // =========================================================

        public TicketPriorityResponse restore(Long id) {

                TicketPriority priority = ticketPriorityRepository
                                .findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket priority not found with id: " + id));

                priority.setDeletedAt(null);

                return toResponse(
                                ticketPriorityRepository.save(priority));
        }

        // =========================================================
        // PERMANENT DELETE
        // =========================================================

        public void permanentDelete(Long id) {

                if (!ticketPriorityRepository.existsById(id)) {
                        throw new RuntimeException(
                                        "Ticket priority not found with id: " + id);
                }

                ticketPriorityRepository.deleteById(id);
        }

        // =========================================================
        // VALIDATE NAME - CREATE
        // =========================================================

        private void validateNameForCreate(String name) {

                if (name == null || name.isBlank()) {
                        throw new RuntimeException(
                                        "Ticket priority name is required");
                }

                if (ticketPriorityRepository.existsByName(name.trim())) {
                        throw new RuntimeException(
                                        "Ticket priority already exists with name: "
                                                        + name);
                }
        }

        // =========================================================
        // VALIDATE NAME - UPDATE
        // =========================================================

        private void validateNameForUpdate(
                        String name,
                        Long id) {

                if (name == null || name.isBlank()) {
                        throw new RuntimeException(
                                        "Ticket priority name is required");
                }

                if (ticketPriorityRepository
                                .existsByNameAndIdNot(name.trim(), id)) {

                        throw new RuntimeException(
                                        "Ticket priority already exists with name: "
                                                        + name);
                }
        }

        // =========================================================
        // CLEAR ALL DEFAULT PRIORITIES
        // =========================================================

        private void clearDefaultPriorities() {

                List<TicketPriority> defaults = ticketPriorityRepository
                                .findByIsDefaultTrue();

                for (TicketPriority priority : defaults) {

                        priority.setIsDefault(false);
                }

                ticketPriorityRepository.saveAll(defaults);
        }

        // =========================================================
        // CLEAR DEFAULT EXCEPT CURRENT ID
        // =========================================================

        private void clearDefaultPriorities(Long exceptId) {

                List<TicketPriority> defaults = ticketPriorityRepository
                                .findByIsDefaultTrue();

                for (TicketPriority priority : defaults) {

                        if (!priority.getId().equals(exceptId)) {
                                priority.setIsDefault(false);
                        }
                }

                ticketPriorityRepository.saveAll(defaults);
        }

        // =========================================================
        // REORDER
        // =========================================================
        public List<TicketPriorityResponse> reorder(List<Long> ids) {
                if (ids == null || ids.isEmpty())
                        throw new RuntimeException("Priority order is required");
                for (int i = 0; i < ids.size(); i++) {
                        TicketPriority priority = ticketPriorityRepository.findById(ids.get(i))
                                        .orElseThrow(() -> new RuntimeException("Ticket priority not found"));
                        priority.setDisplayOrder(i);
                }
                return getAll();
        }

        // =========================================================
        // ENTITY -> RESPONSE
        // =========================================================

        private TicketPriorityResponse toResponse(
                        TicketPriority priority) {

                return TicketPriorityResponse.builder()
                                .id(priority.getId())
                                .name(priority.getName())
                                .color(priority.getColor())
                                .isDefault(priority.getIsDefault())
                                .displayOrder(priority.getDisplayOrder())
                                .deletedAt(priority.getDeletedAt())
                                .createdAt(priority.getCreatedAt())
                                .updatedAt(priority.getUpdatedAt())
                                .build();
        }
}