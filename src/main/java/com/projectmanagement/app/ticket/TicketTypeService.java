package com.projectmanagement.app.ticket;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeService(
            TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getAllTicketTypes() {

        return ticketTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET ACTIVE
    // =========================================================

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getActiveTicketTypes() {

        return ticketTypeRepository
                .findByDeletedAtIsNull()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketTypeById(
            Long id) {

        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket type not found with id: "
                                + id));

        return toResponse(ticketType);
    }

    // =========================================================
    // GET BY NAME
    // =========================================================

    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketTypeByName(
            String name) {

        TicketType ticketType = ticketTypeRepository
                .findByNameAndDeletedAtIsNull(name)
                .orElseThrow(() -> new RuntimeException(
                        "Active ticket type not found with name: "
                                + name));

        return toResponse(ticketType);
    }

    // =========================================================
    // GET DEFAULT
    // =========================================================

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getDefaultTicketTypes() {

        return ticketTypeRepository
                .findByIsDefaultTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET BY ICON
    // =========================================================

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesByIcon(
            String icon) {

        return ticketTypeRepository
                .findByIcon(icon)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET BY COLOR
    // =========================================================

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesByColor(
            String color) {

        return ticketTypeRepository
                .findByColor(color)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // CREATE
    // =========================================================

    public TicketTypeResponse createTicketType(
            TicketTypeRequest request) {

        validateName(
                request.getName(),
                null);

        TicketType ticketType = TicketType.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .isDefault(
                        request.getIsDefault() != null
                                ? request.getIsDefault()
                                : false)
                .build();

        if (Boolean.TRUE.equals(
                ticketType.getIsDefault())) {

            clearExistingDefaultTicketTypes();
        }

        return toResponse(
                ticketTypeRepository.save(ticketType));
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public TicketTypeResponse updateTicketType(
            Long id,
            TicketTypeRequest request) {

        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket type not found with id: "
                                + id));

        validateName(
                request.getName(),
                id);

        ticketType.setName(request.getName());
        ticketType.setIcon(request.getIcon());
        ticketType.setColor(request.getColor());

        if (request.getIsDefault() != null) {

            ticketType.setIsDefault(
                    request.getIsDefault());

            if (Boolean.TRUE.equals(
                    request.getIsDefault())) {

                clearExistingDefaultTicketTypes(id);
            }
        }

        return toResponse(
                ticketTypeRepository.save(ticketType));
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    public void deleteTicketType(Long id) {

        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket type not found with id: "
                                + id));

        ticketType.setDeletedAt(
                LocalDateTime.now());

        ticketTypeRepository.save(ticketType);
    }

    // =========================================================
    // RESTORE
    // =========================================================

    public TicketTypeResponse restoreTicketType(
            Long id) {

        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket type not found with id: "
                                + id));

        ticketType.setDeletedAt(null);

        return toResponse(
                ticketTypeRepository.save(ticketType));
    }

    // =========================================================
    // PERMANENT DELETE
    // =========================================================

    public void permanentlyDeleteTicketType(
            Long id) {

        if (!ticketTypeRepository.existsById(id)) {

            throw new RuntimeException(
                    "Ticket type not found with id: " + id);
        }

        ticketTypeRepository.deleteById(id);
    }

    // =========================================================
    // VALIDATE NAME
    // =========================================================

    private void validateName(
            String name,
            Long currentId) {

        boolean exists;

        if (currentId == null) {

            exists = ticketTypeRepository
                    .existsByName(name);

        } else {

            exists = ticketTypeRepository
                    .existsByNameAndIdNot(
                            name,
                            currentId);
        }

        if (exists) {

            throw new RuntimeException(
                    "Ticket type already exists with name: "
                            + name);
        }
    }

    // =========================================================
    // CLEAR DEFAULT TYPES
    // =========================================================

    private void clearExistingDefaultTicketTypes() {

        clearExistingDefaultTicketTypes(null);
    }

    private void clearExistingDefaultTicketTypes(
            Long excludedId) {

        List<TicketType> defaults = ticketTypeRepository
                .findByIsDefaultTrue();

        for (TicketType ticketType : defaults) {

            if (excludedId == null ||
                    !ticketType.getId()
                            .equals(excludedId)) {

                ticketType.setIsDefault(false);

                ticketTypeRepository.save(
                        ticketType);
            }
        }
    }

    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private TicketTypeResponse toResponse(
            TicketType ticketType) {

        return TicketTypeResponse.builder()
                .id(ticketType.getId())
                .name(ticketType.getName())
                .icon(ticketType.getIcon())
                .color(ticketType.getColor())
                .isDefault(ticketType.getIsDefault())
                .deletedAt(ticketType.getDeletedAt())
                .createdAt(ticketType.getCreatedAt())
                .updatedAt(ticketType.getUpdatedAt())
                .build();
    }
}