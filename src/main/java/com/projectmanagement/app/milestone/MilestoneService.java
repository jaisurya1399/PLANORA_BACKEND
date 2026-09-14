package com.projectmanagement.app.milestone;

import java.time.LocalDate;
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
public class MilestoneService {

        private final MilestoneRepository milestoneRepository;
        private final ProjectRepository projectRepository;
        private final TicketRepository ticketRepository;

        // =========================================================
        // CREATE
        // =========================================================

        public MilestoneResponse create(
                        Long projectId,
                        MilestoneRequest request) {

                Project project = getProject(projectId);

                validateDates(
                                request.getStartDate(),
                                request.getDueDate());

                String name = normalizeName(
                                request.getName());

                if (milestoneRepository
                                .existsByProjectIdAndNameIgnoreCase(
                                                projectId,
                                                name)) {

                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Milestone already exists in this project");
                }

                Milestone milestone = Milestone.builder()
                                .name(name)
                                .description(
                                                normalizeDescription(
                                                                request.getDescription()))
                                .project(project)
                                .startDate(request.getStartDate())
                                .dueDate(request.getDueDate())
                                .status(MilestoneStatus.PLANNED)
                                .progressPercent(
                                                normalizeProgress(
                                                                request.getProgressPercent()))
                                .build();

                milestone = milestoneRepository.save(
                                milestone);

                return toResponse(milestone);
        }

        // =========================================================
        // UPDATE
        // =========================================================

        public MilestoneResponse update(
                        Long projectId,
                        Long milestoneId,
                        MilestoneRequest request) {

                Milestone milestone = getMilestone(
                                projectId,
                                milestoneId);

                validateDates(
                                request.getStartDate(),
                                request.getDueDate());

                String name = normalizeName(
                                request.getName());

                if (milestoneRepository
                                .existsByProjectIdAndNameIgnoreCaseAndIdNot(
                                                projectId,
                                                name,
                                                milestoneId)) {

                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Another milestone with the same name already exists");
                }

                if (milestone.getStatus() == MilestoneStatus.COMPLETED
                                &&
                                request.getProgressPercent() != null
                                &&
                                request.getProgressPercent() < 100) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Completed milestone must have 100% progress");
                }

                milestone.setName(name);

                milestone.setDescription(
                                normalizeDescription(
                                                request.getDescription()));

                milestone.setStartDate(
                                request.getStartDate());

                milestone.setDueDate(
                                request.getDueDate());

                if (request.getProgressPercent() != null) {

                        milestone.setProgressPercent(
                                        normalizeProgress(
                                                        request.getProgressPercent()));
                }

                if (milestone.getProgressPercent() == 100
                                &&
                                milestone.getStatus() != MilestoneStatus.CANCELLED) {

                        milestone.setStatus(
                                        MilestoneStatus.COMPLETED);
                }

                return toResponse(
                                milestoneRepository.save(
                                                milestone));
        }

        // =========================================================
        // GET
        // =========================================================

        @Transactional(readOnly = true)
        public MilestoneResponse get(
                        Long projectId,
                        Long milestoneId) {

                return toResponse(
                                getMilestone(
                                                projectId,
                                                milestoneId));
        }

        // =========================================================
        // LIST
        // =========================================================

        @Transactional(readOnly = true)
        public List<MilestoneResponse> getProjectMilestones(
                        Long projectId) {

                getProject(projectId);

                return milestoneRepository
                                .findByProjectIdOrderByDueDateAscNameAsc(
                                                projectId)
                                .stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        // =========================================================
        // STATUS
        // =========================================================

        public MilestoneResponse updateStatus(
                        Long projectId,
                        Long milestoneId,
                        MilestoneStatus newStatus) {

                Milestone milestone = getMilestone(
                                projectId,
                                milestoneId);

                validateStatusTransition(
                                milestone.getStatus(),
                                newStatus);

                milestone.setStatus(newStatus);

                if (newStatus == MilestoneStatus.COMPLETED) {

                        milestone.setProgressPercent(100);
                }

                if (newStatus == MilestoneStatus.PLANNED
                                &&
                                milestone.getProgressPercent() == 100) {

                        milestone.setProgressPercent(0);
                }

                return toResponse(
                                milestoneRepository.save(
                                                milestone));
        }

        // =========================================================
        // PROGRESS
        // =========================================================

        public MilestoneResponse updateProgress(
                        Long projectId,
                        Long milestoneId,
                        Integer progress) {

                Milestone milestone = getMilestone(
                                projectId,
                                milestoneId);

                if (progress == null ||
                                progress < 0 ||
                                progress > 100) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Progress must be between 0 and 100");
                }

                if (milestone.getStatus() == MilestoneStatus.CANCELLED) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Cancelled milestone cannot be updated");
                }

                milestone.setProgressPercent(progress);

                if (progress == 100) {

                        milestone.setStatus(
                                        MilestoneStatus.COMPLETED);

                } else if (progress > 0 &&
                                milestone.getStatus() == MilestoneStatus.PLANNED) {

                        milestone.setStatus(
                                        MilestoneStatus.IN_PROGRESS);
                }

                return toResponse(
                                milestoneRepository.save(
                                                milestone));
        }

        // =========================================================
        // DELETE
        // =========================================================

        public void delete(
                        Long projectId,
                        Long milestoneId) {

                Milestone milestone = getMilestone(
                                projectId,
                                milestoneId);

                /*
                 * Tickets should go back to unassigned milestone.
                 */
                List<Ticket> tickets = ticketRepository
                                .findByMilestoneIdOrderByOrderAscIdAsc(
                                                milestoneId);

                for (Ticket ticket : tickets) {
                        ticket.setMilestone(null);
                }

                ticketRepository.saveAll(tickets);

                milestoneRepository.delete(
                                milestone);
        }

        // =========================================================
        // ASSIGN TICKET
        // =========================================================

        public MilestoneResponse assignTicket(
                        Long projectId,
                        Long milestoneId,
                        Long ticketId) {

                Milestone milestone = getMilestone(
                                projectId,
                                milestoneId);

                Ticket ticket = getTicket(ticketId);

                validateTicketProject(
                                ticket,
                                projectId);

                if (milestone.getStatus() == MilestoneStatus.COMPLETED
                                ||
                                milestone.getStatus() == MilestoneStatus.CANCELLED) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Tickets cannot be assigned to a completed or cancelled milestone");
                }

                ticket.setMilestone(milestone);

                ticketRepository.save(ticket);

                return toResponse(milestone);
        }

        // =========================================================
        // REMOVE TICKET
        // =========================================================

        public void removeTicket(
                        Long projectId,
                        Long milestoneId,
                        Long ticketId) {

                getMilestone(
                                projectId,
                                milestoneId);

                Ticket ticket = getTicket(ticketId);

                validateTicketProject(
                                ticket,
                                projectId);

                if (ticket.getMilestone() == null
                                ||
                                !milestoneId.equals(
                                                ticket.getMilestone().getId())) {

                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Ticket is not assigned to this milestone");
                }

                ticket.setMilestone(null);

                ticketRepository.save(ticket);
        }

        // =========================================================
        // GET MILESTONE TICKETS
        // =========================================================

        @Transactional(readOnly = true)
        public List<MilestoneTicketResponse> getTickets(
                        Long projectId,
                        Long milestoneId) {

                getMilestone(
                                projectId,
                                milestoneId);

                return ticketRepository
                                .findByMilestoneIdOrderByOrderAscIdAsc(
                                                milestoneId)
                                .stream()
                                .map(this::toTicketResponse)
                                .collect(Collectors.toList());
        }

        // =========================================================
        // STATISTICS
        // =========================================================

        @Transactional(readOnly = true)
        public MilestoneStatisticsResponse getStatistics(
                        Long projectId,
                        Long milestoneId) {

                Milestone milestone = getMilestone(
                                projectId,
                                milestoneId);

                long total = ticketRepository.countByMilestoneId(
                                milestoneId);

                long completed = ticketRepository.countCompletedByMilestoneId(
                                milestoneId);

                long open = Math.max(
                                total - completed,
                                0);

                boolean overdue = milestone.getDueDate() != null
                                &&
                                milestone.getDueDate()
                                                .isBefore(LocalDate.now())
                                &&
                                milestone.getStatus() != MilestoneStatus.COMPLETED
                                &&
                                milestone.getStatus() != MilestoneStatus.CANCELLED;

                return MilestoneStatisticsResponse.builder()
                                .milestoneId(milestone.getId())
                                .milestoneName(milestone.getName())
                                .totalTickets(total)
                                .completedTickets(completed)
                                .openTickets(open)
                                .progressPercent(
                                                milestone.getProgressPercent())
                                .overdue(overdue)
                                .build();
        }

        // =========================================================
        // HELPERS
        // =========================================================

        private Project getProject(
                        Long projectId) {

                return projectRepository
                                .findById(projectId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Project not found"));
        }

        private Milestone getMilestone(
                        Long projectId,
                        Long milestoneId) {

                return milestoneRepository
                                .findByIdAndProjectId(
                                                milestoneId,
                                                projectId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Milestone not found in this project"));
        }

        private Ticket getTicket(
                        Long ticketId) {

                return ticketRepository
                                .findById(ticketId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Ticket not found"));
        }

        private void validateTicketProject(
                        Ticket ticket,
                        Long projectId) {

                if (ticket.getProject() == null
                                ||
                                ticket.getProject().getId() == null
                                ||
                                !ticket.getProject()
                                                .getId()
                                                .equals(projectId)) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Ticket does not belong to this project");
                }
        }

        private void validateDates(
                        LocalDate startDate,
                        LocalDate dueDate) {

                if (startDate != null
                                &&
                                dueDate != null
                                &&
                                dueDate.isBefore(startDate)) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Due date cannot be before start date");
                }
        }

        private void validateStatusTransition(
                        MilestoneStatus current,
                        MilestoneStatus target) {

                if (target == null) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Status is required");
                }

                if (current == target) {
                        return;
                }

                if (current == MilestoneStatus.COMPLETED) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Completed milestone cannot be reopened");
                }

                if (current == MilestoneStatus.CANCELLED) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Cancelled milestone cannot be reopened");
                }

                if (current == MilestoneStatus.PLANNED
                                &&
                                (target == MilestoneStatus.IN_PROGRESS
                                                ||
                                                target == MilestoneStatus.CANCELLED
                                                ||
                                                target == MilestoneStatus.COMPLETED)) {
                        return;
                }

                if (current == MilestoneStatus.IN_PROGRESS
                                &&
                                (target == MilestoneStatus.COMPLETED
                                                ||
                                                target == MilestoneStatus.CANCELLED)) {
                        return;
                }

                throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid milestone status transition");
        }

        private String normalizeName(
                        String name) {

                if (name == null
                                ||
                                name.isBlank()) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Milestone name is required");
                }

                return name.trim();
        }

        private String normalizeDescription(
                        String description) {

                if (description == null) {
                        return null;
                }

                String value = description.trim();

                return value.isEmpty()
                                ? null
                                : value;
        }

        private Integer normalizeProgress(
                        Integer progress) {

                if (progress == null) {
                        return 0;
                }

                if (progress < 0 || progress > 100) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Progress must be between 0 and 100");
                }

                return progress;
        }

        // =========================================================
        // RESPONSE MAPPING
        // =========================================================

        private MilestoneResponse toResponse(
                        Milestone milestone) {

                long total = ticketRepository.countByMilestoneId(
                                milestone.getId());

                long completed = ticketRepository
                                .countCompletedByMilestoneId(
                                                milestone.getId());

                return MilestoneResponse.builder()
                                .id(milestone.getId())
                                .name(milestone.getName())
                                .description(milestone.getDescription())
                                .projectId(
                                                milestone.getProject().getId())
                                .projectName(
                                                milestone.getProject().getName())
                                .startDate(milestone.getStartDate())
                                .dueDate(milestone.getDueDate())
                                .status(milestone.getStatus())
                                .progressPercent(
                                                milestone.getProgressPercent())
                                .totalTickets(total)
                                .completedTickets(completed)
                                .createdAt(milestone.getCreatedAt())
                                .updatedAt(milestone.getUpdatedAt())
                                .build();
        }

        private MilestoneTicketResponse toTicketResponse(
                        Ticket ticket) {

                return MilestoneTicketResponse.builder()
                                .id(ticket.getId())
                                .code(ticket.getCode())
                                .name(ticket.getName())
                                .projectId(
                                                ticket.getProject() != null
                                                                ? ticket.getProject().getId()
                                                                : null)
                                .projectName(
                                                ticket.getProject() != null
                                                                ? ticket.getProject().getName()
                                                                : null)
                                .ownerId(
                                                ticket.getOwner() != null
                                                                ? ticket.getOwner().getId()
                                                                : null)
                                .ownerName(
                                                ticket.getOwner() != null
                                                                ? ticket.getOwner().getName()
                                                                : null)
                                .responsibleId(
                                                ticket.getResponsible() != null
                                                                ? ticket.getResponsible().getId()
                                                                : null)
                                .responsibleName(
                                                ticket.getResponsible() != null
                                                                ? ticket.getResponsible().getName()
                                                                : null)
                                .statusId(
                                                ticket.getStatus() != null
                                                                ? ticket.getStatus().getId()
                                                                : null)
                                .statusName(
                                                ticket.getStatus() != null
                                                                ? ticket.getStatus().getName()
                                                                : null)
                                .priorityId(
                                                ticket.getPriority() != null
                                                                ? ticket.getPriority().getId()
                                                                : null)
                                .priorityName(
                                                ticket.getPriority() != null
                                                                ? ticket.getPriority().getName()
                                                                : null)
                                .estimation(ticket.getEstimation())
                                .build();
        }
}