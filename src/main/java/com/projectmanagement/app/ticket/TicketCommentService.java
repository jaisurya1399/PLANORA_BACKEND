package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.audit.AuditService;
import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.notification.TicketNotificationService;
import com.projectmanagement.app.realtime.RealtimeEventService;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;
    private final TicketSubscriberRepository ticketSubscriberRepository;
    private final TicketNotificationService ticketNotificationService;
    private final AuditService auditService;
    private final RealtimeEventService realtimeEvents;

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> getAll() {
        return ticketCommentRepository.findAll()
                .stream()
                .filter(comment -> projectAccessService.canView(comment.getTicket().getProject()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> getActive() {
        return ticketCommentRepository.findByDeletedAtIsNull()
                .stream()
                .filter(comment -> projectAccessService.canView(comment.getTicket().getProject()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketCommentResponse getById(Long id) {
        TicketComment comment = ticketCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket comment not found with id: " + id));

        projectAccessService.requireView(comment.getTicket().getProject());
        return mapToResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> getByTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        projectAccessService.requireView(ticket.getProject());

        return ticketCommentRepository
                .findByTicketIdAndDeletedAtIsNullOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> getByUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        return ticketCommentRepository.findByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .filter(comment -> projectAccessService.canView(comment.getTicket().getProject()))
                .map(this::mapToResponse)
                .toList();
    }

    public TicketCommentResponse create(TicketCommentRequest request) {

        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new RuntimeException(
                        "Ticket not found with id: " + request.getTicketId()));

        projectAccessService.requireEditor(ticket.getProject());
        User user = currentUserService.getCurrentUser();

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(user)
                .content(request.getContent())
                .build();

        TicketComment saved = ticketCommentRepository.save(comment);
        subscribeIfNeeded(ticket, user);
        ticketNotificationService.notifyComment(ticket, user, saved.getContent());
        auditService.record(ticket.getProject(), ticket, "COMMENT_CREATED", "TICKET_COMMENT", saved.getId(),
                java.util.Map.of("commentId", saved.getId()));
        realtimeEvents.publishProject(ticket.getProject().getId(), "comment.created",
                java.util.Map.of("ticketId", ticket.getId(), "commentId", saved.getId()));
        return mapToResponse(saved);
    }

    public TicketCommentResponse update(
            Long id,
            TicketCommentRequest request) {

        TicketComment comment = ticketCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket comment not found with id: " + id));

        projectAccessService.requireEditor(comment.getTicket().getProject());
        if (!comment.getUser().getId().equals(currentUserService.getCurrentUserId()))
            throw new RuntimeException("Only the comment author can edit this comment");
        if (!comment.getTicket().getId().equals(request.getTicketId()))
            throw new RuntimeException("A comment cannot be moved to another ticket");
        comment.setContent(request.getContent());

        TicketComment updated = ticketCommentRepository.save(comment);
        auditService.record(updated.getTicket().getProject(), updated.getTicket(), "COMMENT_UPDATED", "TICKET_COMMENT",
                updated.getId(), java.util.Map.of("commentId", updated.getId()));
        realtimeEvents.publishProject(updated.getTicket().getProject().getId(), "comment.updated",
                java.util.Map.of("ticketId", updated.getTicket().getId(), "commentId", updated.getId()));
        return mapToResponse(updated);
    }

    public void softDelete(Long id) {

        TicketComment comment = ticketCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket comment not found with id: " + id));

        projectAccessService.requireEditor(comment.getTicket().getProject());
        if (!comment.getUser().getId().equals(currentUserService.getCurrentUserId()))
            throw new RuntimeException("Only the comment author can delete this comment");
        comment.setDeletedAt(java.time.LocalDateTime.now());

        ticketCommentRepository.save(comment);
        auditService.record(comment.getTicket().getProject(), comment.getTicket(), "COMMENT_DELETED", "TICKET_COMMENT",
                comment.getId(), java.util.Map.of("commentId", comment.getId()));
        realtimeEvents.publishProject(comment.getTicket().getProject().getId(), "comment.deleted",
                java.util.Map.of("ticketId", comment.getTicket().getId(), "commentId", comment.getId()));
    }

    public void restore(Long id) {

        TicketComment comment = ticketCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket comment not found with id: " + id));

        comment.setDeletedAt(null);

        ticketCommentRepository.save(comment);
    }

    public void permanentDelete(Long id) {

        if (!ticketCommentRepository.existsById(id)) {
            throw new RuntimeException(
                    "Ticket comment not found with id: " + id);
        }

        ticketCommentRepository.deleteById(id);
    }

    public void deleteByTicket(Long ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException(
                    "Ticket not found with id: " + ticketId);
        }

        ticketCommentRepository.deleteByTicketId(ticketId);
    }

    public void deleteByUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId);
        }

        ticketCommentRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countByTicket(Long ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException(
                    "Ticket not found with id: " + ticketId);
        }

        return ticketCommentRepository
                .countByTicketIdAndDeletedAtIsNull(ticketId);
    }

    private TicketCommentResponse mapToResponse(TicketComment comment) {

        Ticket ticket = comment.getTicket();
        User user = comment.getUser();

        return TicketCommentResponse.builder()
                .id(comment.getId())

                .ticketId(ticket.getId())
                .ticketName(ticket.getName())
                .ticketCode(ticket.getCode())

                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())

                .content(comment.getContent())

                .deletedAt(comment.getDeletedAt())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())

                .build();
    }

    private void subscribeIfNeeded(Ticket ticket, User user) {
        if (!ticketSubscriberRepository.existsByTicketIdAndUserId(ticket.getId(), user.getId()))
            ticketSubscriberRepository.save(TicketSubscriber.builder().ticket(ticket).user(user).build());
    }
}
