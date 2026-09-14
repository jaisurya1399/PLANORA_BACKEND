package com.projectmanagement.app.label;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LabelService {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;

    // =========================================================
    // CREATE LABEL
    // =========================================================

    public LabelResponse create(
            Long projectId,
            LabelRequest request) {

        Project project = getProject(projectId);

        String name = normalizeName(request.getName());

        if (labelRepository.existsByProjectIdAndNameIgnoreCase(
                projectId,
                name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Label already exists in this project");
        }

        Label label = Label.builder()
                .name(name)
                .color(normalizeColor(request.getColor()))
                .description(normalizeDescription(request.getDescription()))
                .project(project)
                .build();

        label = labelRepository.save(label);

        return toResponse(label);
    }

    // =========================================================
    // UPDATE LABEL
    // =========================================================

    public LabelResponse update(
            Long projectId,
            Long labelId,
            LabelRequest request) {

        Label label = getLabel(projectId, labelId);

        String name = normalizeName(request.getName());

        if (labelRepository
                .existsByProjectIdAndNameIgnoreCaseAndIdNot(
                        projectId,
                        name,
                        labelId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another label with the same name already exists");
        }

        label.setName(name);

        if (request.getColor() != null &&
                !request.getColor().isBlank()) {

            label.setColor(
                    normalizeColor(request.getColor()));
        }

        label.setDescription(
                normalizeDescription(request.getDescription()));

        return toResponse(labelRepository.save(label));
    }

    // =========================================================
    // GET LABEL
    // =========================================================

    @Transactional(readOnly = true)
    public LabelResponse get(
            Long projectId,
            Long labelId) {

        Label label = getLabel(projectId, labelId);

        return toResponse(label);
    }

    // =========================================================
    // LIST PROJECT LABELS
    // =========================================================

    @Transactional(readOnly = true)
    public List<LabelResponse> getProjectLabels(
            Long projectId) {

        getProject(projectId);

        return labelRepository
                .findByProjectIdOrderByNameAsc(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // DELETE LABEL
    // =========================================================

    public void delete(
            Long projectId,
            Long labelId) {

        Label label = getLabel(projectId, labelId);

        labelRepository.delete(label);
    }

    // =========================================================
    // ADD LABEL TO TICKET
    // =========================================================

    public LabelResponse addLabelToTicket(
            Long projectId,
            Long ticketId,
            Long labelId) {

        Label label = getLabel(projectId, labelId);

        Ticket ticket = getTicket(ticketId);

        validateTicketProject(
                ticket,
                projectId);

        if (ticket.getLabels() == null) {
            ticket.setLabels(new HashSet<>());
        }

        ticket.getLabels().add(label);

        ticketRepository.save(ticket);

        return toResponse(label);
    }

    // =========================================================
    // REMOVE LABEL FROM TICKET
    // =========================================================

    public void removeLabelFromTicket(
            Long projectId,
            Long ticketId,
            Long labelId) {

        Label label = getLabel(projectId, labelId);

        Ticket ticket = getTicket(ticketId);

        validateTicketProject(
                ticket,
                projectId);

        if (ticket.getLabels() != null) {
            ticket.getLabels().remove(label);
        }

        ticketRepository.save(ticket);
    }

    // =========================================================
    // GET TICKET LABELS
    // =========================================================

    @Transactional(readOnly = true)
    public List<LabelResponse> getTicketLabels(
            Long projectId,
            Long ticketId) {

        Ticket ticket = getTicket(ticketId);

        validateTicketProject(
                ticket,
                projectId);

        return ticket.getLabels()
                .stream()
                .map(this::toResponse)
                .sorted(
                        (a, b) -> a.getName()
                                .compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET TICKETS BY LABEL
    // =========================================================

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByLabel(
            Long projectId,
            Long labelId) {

        getLabel(projectId, labelId);

        return ticketRepository.findTicketsByLabelId(
                labelId);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Project getProject(Long projectId) {

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found"));
    }

    private Label getLabel(
            Long projectId,
            Long labelId) {

        return labelRepository
                .findByIdAndProjectId(
                        labelId,
                        projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Label not found in this project"));
    }

    private Ticket getTicket(Long ticketId) {

        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ticket not found"));
    }

    private void validateTicketProject(
            Ticket ticket,
            Long projectId) {

        if (ticket.getProject() == null ||
                ticket.getProject().getId() == null ||
                !ticket.getProject().getId().equals(projectId)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ticket does not belong to this project");
        }
    }

    private String normalizeName(String name) {

        if (name == null || name.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Label name is required");
        }

        return name.trim();
    }

    private String normalizeColor(String color) {

        if (color == null || color.isBlank()) {
            return "#1976D2";
        }

        String normalized = color.trim();

        if (!normalized.matches(
                "^#[0-9A-Fa-f]{6}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid label color");
        }

        return normalized;
    }

    private String normalizeDescription(
            String description) {

        if (description == null) {
            return null;
        }

        String value = description.trim();

        return value.isEmpty() ? null : value;
    }

    private LabelResponse toResponse(Label label) {

        long ticketCount = labelRepository.countTicketsByLabelId(
                label.getId());

        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .description(label.getDescription())
                .projectId(
                        label.getProject().getId())
                .projectName(
                        label.getProject().getName())
                .ticketCount(ticketCount)
                .createdAt(label.getCreatedAt())
                .updatedAt(label.getUpdatedAt())
                .build();
    }
}