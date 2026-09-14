package com.projectmanagement.app.notification;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.projectmanagement.app.notificationscheme.NotificationScheme;
import com.projectmanagement.app.notificationscheme.NotificationSchemeRepository;
import com.projectmanagement.app.notificationscheme.NotificationSchemeRule;
import com.projectmanagement.app.notificationscheme.NotificationSchemeRuleRepository;
import com.projectmanagement.app.project.ProjectRole;
import com.projectmanagement.app.project.ProjectUserRepository;
import com.projectmanagement.app.realtime.RealtimeEventService;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketStatus;
import com.projectmanagement.app.ticket.TicketSubscriberRepository;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
public class TicketNotificationService {
        private static final Pattern MENTION = Pattern.compile("@([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})");
        private final NotificationRepository notificationRepository;
        private final UserRepository userRepository;
        private final ProjectUserRepository projectUserRepository;
        private final TicketSubscriberRepository subscriberRepository;
        private final RealtimeEventService realtimeEvents;
        private final ApplicationEventPublisher eventPublisher;
        private final NotificationPreferenceService preferenceService;
        private final NotificationSchemeRepository notificationSchemeRepository;
        private final NotificationSchemeRuleRepository notificationSchemeRuleRepository;

        public TicketNotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                        ProjectUserRepository projectUserRepository, TicketSubscriberRepository subscriberRepository,
                        RealtimeEventService realtimeEvents, ApplicationEventPublisher eventPublisher,
                        NotificationPreferenceService preferenceService,
                        NotificationSchemeRepository notificationSchemeRepository,
                        NotificationSchemeRuleRepository notificationSchemeRuleRepository) {
                this.notificationRepository = notificationRepository;
                this.userRepository = userRepository;
                this.projectUserRepository = projectUserRepository;
                this.subscriberRepository = subscriberRepository;
                this.realtimeEvents = realtimeEvents;
                this.eventPublisher = eventPublisher;
                this.preferenceService = preferenceService;
                this.notificationSchemeRepository = notificationSchemeRepository;
                this.notificationSchemeRuleRepository = notificationSchemeRuleRepository;
        }

        public void notifyComment(Ticket ticket, User author, String content) {
                Set<User> recipients = new HashSet<>();
                if (ticket.getResponsible() != null)
                        recipients.add(ticket.getResponsible());
                subscriberRepository.findByTicketId(ticket.getId())
                                .forEach(subscription -> recipients.add(subscription.getUser()));
                Matcher matcher = MENTION.matcher(content);
                Set<Long> mentionedIds = new HashSet<>();
                while (matcher.find())
                        userRepository.findByEmail(matcher.group(1).toLowerCase())
                                        .filter(user -> isProjectUser(ticket, user))
                                        .ifPresent(user -> {
                                                mentionedIds.add(user.getId());
                                                recipients.add(user);
                                        });
                recipients.stream().filter(user -> !user.getId().equals(author.getId())).forEach(user -> {
                        boolean mentioned = mentionedIds.contains(user.getId());
                        save(user, mentioned ? "TICKET_MENTION" : "TICKET_COMMENT",
                                        mentioned ? "MENTIONED" : "SUBSCRIBER", ticket,
                                        mentioned ? author.getName() + " mentioned you on " + ticket.getCode()
                                                        : author.getName() + " commented on " + ticket.getCode());
                });
        }

        public void notifyStatusChange(Ticket ticket, User actor, TicketStatus oldStatus, TicketStatus newStatus) {
                Set<User> recipients = new HashSet<>();
                if (ticket.getResponsible() != null)
                        recipients.add(ticket.getResponsible());
                subscriberRepository.findByTicketId(ticket.getId())
                                .forEach(subscription -> recipients.add(subscription.getUser()));
                recipients.stream().filter(user -> !user.getId().equals(actor.getId()))
                                .forEach(user -> save(user, "TICKET_STATUS_CHANGED", "RESPONSIBLE", ticket,
                                                actor.getName() + " moved " + ticket.getCode() + " to "
                                                                + newStatus.getName()));
        }

        public void notifyAssignment(Ticket ticket, User actor) {
                if (ticket.getResponsible() != null && !ticket.getResponsible().getId().equals(actor.getId()))
                        save(ticket.getResponsible(), "TICKET_ASSIGNED", "ASSIGNEE", ticket,
                                        actor.getName() + " assigned " + ticket.getCode() + " to you");
        }

        public void notifyManualReminder(Ticket ticket, User actor) {
                if (ticket.getResponsible() != null && !ticket.getResponsible().getId().equals(actor.getId())) {
                        save(ticket.getResponsible(), "TICKET_REMINDER", "ASSIGNEE", ticket,
                                        actor.getName() + " sent you a reminder for " + ticket.getCode());
                }
        }

        public void notifyProjectAdminsOfOverdue(Ticket ticket) {
                Set<User> recipients = new HashSet<>();

                if (ticket.getProject() != null && ticket.getProject().getOwner() != null) {
                        recipients.add(ticket.getProject().getOwner());
                }

                if (ticket.getProject() != null) {
                        projectUserRepository.findByProjectIdAndRoleIgnoreCase(
                                        ticket.getProject().getId(), ProjectRole.PROJECT_ADMIN.name())
                                        .forEach(member -> recipients.add(member.getUser()));
                }

                String assignee = ticket.getResponsible() == null
                                ? "Unassigned"
                                : ticket.getResponsible().getName();

                recipients.forEach(user -> save(
                                user,
                                "TICKET_OVERDUE",
                                "PROJECT_ADMIN",
                                ticket,
                                ticket.getCode() + " is overdue. Due date: "
                                                + ticket.getDueDate() + ". Assignee: " + assignee));
        }

        private boolean isProjectUser(Ticket ticket, User user) {
                return ticket.getProject().getOwner().getId().equals(user.getId())
                                || projectUserRepository.existsByProjectIdAndUserId(ticket.getProject().getId(),
                                                user.getId());
        }

        /**
         * Persists an in-app notification when allowed by both the project's
         * notification scheme and the recipient's personal preference.
         * The domain event is published independently so email/web-push can
         * still work when in-app notifications are disabled.
         */
        private void save(User recipient, String type, String recipientType, Ticket ticket, String message) {
                NotificationSchemeRule rule = findRule(ticket, type, recipientType);

                // If a project has configured rules for this event, obey them.
                // If no rule exists, preserve the default behavior for backwards compatibility.
                boolean schemeInApp = rule == null || rule.isInAppEnabled();
                boolean schemeEmail = rule == null || rule.isEmailEnabled();

                boolean inAppEnabled = schemeInApp
                                && preferenceService.isInAppEnabled(recipient.getId(), type);

                if (inAppEnabled) {
                        Notification notification = notificationRepository.save(
                                        Notification.builder()
                                                        .type(type)
                                                        .notifiableType("USER")
                                                        .notifiableId(recipient.getId())
                                                        .data(buildData(ticket, message))
                                                        .build());

                        realtimeEvents.publishUser(
                                        recipient.getId(),
                                        "notification",
                                        Map.of(
                                                        "id", notification.getId().toString(),
                                                        "type", type,
                                                        "ticketId", ticket.getId(),
                                                        "ticketCode", ticket.getCode(),
                                                        "message", message));
                }

                // The event is consumed by email and push listeners after commit.
                // The listener itself checks the personal preference; the event
                // remains available even when in-app is disabled.
                if (schemeEmail || inAppEnabled) {
                        eventPublisher.publishEvent(new TicketNotificationCreatedEvent(
                                        recipient, type, ticket.getId(), ticket.getCode(), message,
                                        schemeEmail));
                }
        }

        private String buildData(Ticket ticket, String message) {
                String escapedMessage = message
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"");
                String escapedCode = ticket.getCode() == null ? ""
                                : ticket.getCode()
                                                .replace("\\", "\\\\")
                                                .replace("\"", "\\\"");
                return "{\"ticketId\":" + ticket.getId()
                                + ",\"ticketCode\":\"" + escapedCode
                                + "\",\"message\":\"" + escapedMessage + "\"}";
        }

        private NotificationSchemeRule findRule(Ticket ticket, String type, String recipientType) {
                if (ticket.getProject() == null)
                        return null;

                NotificationScheme scheme = notificationSchemeRepository
                                .findByProjectId(ticket.getProject().getId())
                                .orElse(null);

                if (scheme == null || !scheme.isEnabled())
                        return null;

                String schemeEvent = switch (type) {
                        case "TICKET_ASSIGNED" -> "ISSUE_ASSIGNED";
                        default -> type;
                };

                return notificationSchemeRuleRepository.findBySchemeId(scheme.getId())
                                .stream()
                                .filter(r -> schemeEvent.equalsIgnoreCase(r.getEventType())
                                                || type.equalsIgnoreCase(r.getEventType()))
                                .filter(r -> recipientType.equalsIgnoreCase(r.getRecipientType())
                                                || "ALL".equalsIgnoreCase(r.getRecipientType())
                                                || ("ASSIGNEE".equalsIgnoreCase(recipientType)
                                                                && "RESPONSIBLE".equalsIgnoreCase(
                                                                                r.getRecipientType())))
                                .findFirst()
                                .orElse(null);
        }

}
