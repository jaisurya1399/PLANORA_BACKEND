package com.projectmanagement.app.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends ticket notification email only after its database transaction commits.
 */
@Component
@ConditionalOnProperty(name = "app.notification.email.enabled", havingValue = "true")
public class TicketNotificationEmailListener {
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;
    private final NotificationPreferenceService preferenceService;

    public TicketNotificationEmailListener(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.frontend-url}") String frontendUrl,
            NotificationPreferenceService preferenceService) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
        this.preferenceService = preferenceService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void deliver(TicketNotificationCreatedEvent event) {
        if (!event.emailEnabled())
            return;
        if (!preferenceService.isEmailEnabled(event.recipient().getId(), event.type()))
            return;
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(event.recipient().getEmail());
        email.setSubject("[" + event.ticketCode() + "] " + event.type().replace('_', ' '));
        email.setText(event.message() + "\n\nOpen ticket: " + frontendUrl + "/tickets/" + event.ticketId());
        mailSender.send(email);
    }
}
