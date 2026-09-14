package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.project.ProjectAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketRelationService {

        private static final Set<String> ALLOWED_TYPES = Set.of("BLOCKS", "IS_BLOCKED_BY", "DUPLICATES",
                        "IS_DUPLICATED_BY", "RELATES_TO", "CLONES", "IS_CLONED_BY");

        private final TicketRelationRepository ticketRelationRepository;
        private final TicketRepository ticketRepository;
        private final ProjectAccessService projectAccessService;

        // =========================================================
        // GET ALL
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketRelationResponse> getAll() {

                return ticketRelationRepository.findAll()
                                .stream()
                                .filter(relation -> canViewRelation(relation))
                                .map(this::mapToResponse)
                                .toList();
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @Transactional(readOnly = true)
        public TicketRelationResponse getById(Long id) {

                TicketRelation relation = ticketRelationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket relation not found with id: " + id));

                requireViewRelation(relation);

                return mapToResponse(relation);
        }

        // =========================================================
        // GET BY TICKET
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketRelationResponse> getByTicket(
                        Long ticketId) {

                Ticket ticket = getTicket(ticketId);

                projectAccessService.requireView(
                                ticket.getProject());

                return ticketRelationRepository
                                .findByTicketIdOrderBySortAsc(ticketId)
                                .stream()
                                .filter(this::canViewRelation)
                                .map(this::mapToResponse)
                                .toList();
        }

        // =========================================================
        // GET BY RELATION
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketRelationResponse> getByRelation(
                        Long relationId) {

                Ticket ticket = getTicket(relationId);

                projectAccessService.requireView(
                                ticket.getProject());

                return ticketRelationRepository
                                .findByRelationId(relationId)
                                .stream()
                                .filter(this::canViewRelation)
                                .map(this::mapToResponse)
                                .toList();
        }

        // =========================================================
        // GET BY TYPE
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketRelationResponse> getByType(
                        String type) {

                return ticketRelationRepository
                                .findByType(type)
                                .stream()
                                .filter(this::canViewRelation)
                                .map(this::mapToResponse)
                                .toList();
        }

        // =========================================================
        // GET BY TICKET + TYPE
        // =========================================================

        @Transactional(readOnly = true)
        public List<TicketRelationResponse> getByTicketAndType(
                        Long ticketId,
                        String type) {

                Ticket ticket = getTicket(ticketId);

                projectAccessService.requireView(
                                ticket.getProject());

                return ticketRelationRepository
                                .findByTicketIdAndType(
                                                ticketId,
                                                type)
                                .stream()
                                .filter(this::canViewRelation)
                                .map(this::mapToResponse)
                                .toList();
        }

        // =========================================================
        // EXISTS
        // =========================================================

        @Transactional(readOnly = true)
        public boolean exists(
                        Long ticketId,
                        Long relationId,
                        String type) {

                Ticket ticket = getTicket(ticketId);
                Ticket relatedTicket = getTicket(relationId);

                projectAccessService.requireView(
                                ticket.getProject());

                projectAccessService.requireView(
                                relatedTicket.getProject());

                return ticketRelationRepository
                                .existsByTicketIdAndRelationIdAndType(
                                                ticketId,
                                                relationId,
                                                type);
        }

        // =========================================================
        // CREATE
        // =========================================================

        public TicketRelationResponse create(
                        TicketRelationRequest request) {

                validateRequest(request);

                Ticket ticket = getTicket(
                                request.getTicketId());

                Ticket relation = getTicket(
                                request.getRelationId());

                /*
                 * User must have edit access to the source ticket's project.
                 */
                projectAccessService.requireEditor(
                                ticket.getProject());

                /*
                 * Related ticket must also be visible to the user.
                 */
                projectAccessService.requireView(
                                relation.getProject());

                if (ticket.getId().equals(
                                relation.getId())) {

                        throw new RuntimeException(
                                        "A ticket cannot have a relation with itself");
                }

                if (ticketRelationRepository
                                .existsByTicketIdAndRelationIdAndType(
                                                ticket.getId(),
                                                relation.getId(),
                                                request.getType())) {

                        throw new RuntimeException(
                                        "This ticket relation already exists");
                }

                TicketRelation ticketRelation = TicketRelation.builder()
                                .ticket(ticket)
                                .relation(relation)
                                .type(request.getType())
                                .sort(
                                                request.getSort() != null
                                                                ? request.getSort()
                                                                : 1)
                                .build();

                return mapToResponse(
                                ticketRelationRepository.save(
                                                ticketRelation));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        public TicketRelationResponse update(
                        Long id,
                        TicketRelationRequest request) {

                validateRequest(request);

                TicketRelation ticketRelation = ticketRelationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket relation not found with id: "
                                                                + id));

                /*
                 * Existing relation must be accessible before changing it.
                 */
                projectAccessService.requireEditor(
                                ticketRelation.getTicket().getProject());

                Ticket ticket = getTicket(
                                request.getTicketId());

                Ticket relation = getTicket(
                                request.getRelationId());

                projectAccessService.requireEditor(
                                ticket.getProject());

                projectAccessService.requireView(
                                relation.getProject());

                if (ticket.getId().equals(
                                relation.getId())) {

                        throw new RuntimeException(
                                        "A ticket cannot have a relation with itself");
                }

                boolean relationChanged = !ticketRelation.getTicket().getId()
                                .equals(ticket.getId())
                                ||
                                !ticketRelation.getRelation().getId()
                                                .equals(relation.getId())
                                ||
                                !ticketRelation.getType()
                                                .equals(request.getType());

                if (relationChanged
                                && ticketRelationRepository
                                                .existsByTicketIdAndRelationIdAndType(
                                                                ticket.getId(),
                                                                relation.getId(),
                                                                request.getType())) {

                        throw new RuntimeException(
                                        "This ticket relation already exists");
                }

                ticketRelation.setTicket(ticket);
                ticketRelation.setRelation(relation);
                ticketRelation.setType(request.getType());

                if (request.getSort() != null) {
                        ticketRelation.setSort(
                                        request.getSort());
                }

                return mapToResponse(
                                ticketRelationRepository.save(
                                                ticketRelation));
        }

        // =========================================================
        // DELETE
        // =========================================================

        public void delete(Long id) {

                TicketRelation relation = ticketRelationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket relation not found with id: "
                                                                + id));

                projectAccessService.requireEditor(
                                relation.getTicket().getProject());

                ticketRelationRepository.delete(
                                relation);
        }

        // =========================================================
        // DELETE BY TICKET
        // =========================================================

        public void deleteByTicket(
                        Long ticketId) {

                Ticket ticket = getTicket(ticketId);

                projectAccessService.requireEditor(
                                ticket.getProject());

                ticketRelationRepository.deleteByTicketId(
                                ticketId);
        }

        // =========================================================
        // DELETE BY RELATION
        // =========================================================

        public void deleteByRelation(
                        Long relationId) {

                Ticket ticket = getTicket(relationId);

                projectAccessService.requireEditor(
                                ticket.getProject());

                ticketRelationRepository.deleteByRelationId(
                                relationId);
        }

        // =========================================================
        // COUNT BY TICKET
        // =========================================================

        @Transactional(readOnly = true)
        public long countByTicket(
                        Long ticketId) {

                Ticket ticket = getTicket(ticketId);

                projectAccessService.requireView(
                                ticket.getProject());

                return ticketRelationRepository
                                .findByTicketId(ticketId)
                                .stream()
                                .filter(this::canViewRelation)
                                .count();
        }

        // =========================================================
        // COUNT BY RELATION
        // =========================================================

        @Transactional(readOnly = true)
        public long countByRelation(
                        Long relationId) {

                Ticket ticket = getTicket(relationId);

                projectAccessService.requireView(
                                ticket.getProject());

                return ticketRelationRepository
                                .findByRelationId(relationId)
                                .stream()
                                .filter(this::canViewRelation)
                                .count();
        }

        private void validateRequest(TicketRelationRequest request) {
                if (request == null || request.getTicketId() == null || request.getRelationId() == null)
                        throw new IllegalArgumentException("Both ticketId and relationId are required");
                String type = request.getType() == null ? "" : request.getType().trim().toUpperCase();
                if (!ALLOWED_TYPES.contains(type))
                        throw new IllegalArgumentException("Unsupported relationship type: " + type);
                if (request.getTicketId().equals(request.getRelationId()))
                        throw new IllegalArgumentException("A ticket cannot be related to itself");
                if (request.getSort() != null && request.getSort() < 1)
                        throw new IllegalArgumentException("Sort must be positive");
                request.setType(type);
        }

        private Ticket getTicket(
                        Long ticketId) {

                if (ticketId == null
                                || ticketId <= 0) {

                        throw new RuntimeException(
                                        "Valid ticket ID is required");
                }

                return ticketRepository.findById(ticketId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found with id: "
                                                                + ticketId));
        }

        // =========================================================
        // ACCESS VALIDATION
        // =========================================================

        private boolean canViewRelation(
                        TicketRelation relation) {

                if (relation == null
                                || relation.getTicket() == null
                                || relation.getRelation() == null) {

                        return false;
                }

                if (relation.getTicket().getProject() == null
                                || relation.getRelation().getProject() == null) {

                        return false;
                }

                /*
                 * A relation is visible only when both tickets are visible.
                 */
                return projectAccessService.canView(
                                relation.getTicket().getProject())
                                && projectAccessService.canView(
                                                relation.getRelation().getProject());
        }

        private void requireViewRelation(
                        TicketRelation relation) {

                if (relation == null
                                || relation.getTicket() == null
                                || relation.getRelation() == null) {

                        throw new RuntimeException(
                                        "Invalid ticket relation");
                }

                Ticket ticket = relation.getTicket();
                Ticket relatedTicket = relation.getRelation();

                projectAccessService.requireView(
                                ticket.getProject());

                projectAccessService.requireView(
                                relatedTicket.getProject());
        }

        // =========================================================
        // RESPONSE MAPPER
        // =========================================================

        private TicketRelationResponse mapToResponse(
                        TicketRelation relation) {

                Ticket ticket = relation.getTicket();
                Ticket relatedTicket = relation.getRelation();

                return TicketRelationResponse.builder()
                                .id(relation.getId())

                                .ticketId(
                                                ticket != null
                                                                ? ticket.getId()
                                                                : null)

                                .ticketName(
                                                ticket != null
                                                                ? ticket.getName()
                                                                : null)

                                .ticketCode(
                                                ticket != null
                                                                ? ticket.getCode()
                                                                : null)

                                .relationId(
                                                relatedTicket != null
                                                                ? relatedTicket.getId()
                                                                : null)

                                .relationName(
                                                relatedTicket != null
                                                                ? relatedTicket.getName()
                                                                : null)

                                .relationCode(
                                                relatedTicket != null
                                                                ? relatedTicket.getCode()
                                                                : null)

                                .type(relation.getType())
                                .sort(relation.getSort())

                                .createdAt(relation.getCreatedAt())
                                .updatedAt(relation.getUpdatedAt())

                                .build();
        }
}