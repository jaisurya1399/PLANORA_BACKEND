package com.projectmanagement.app.notification;

import com.projectmanagement.app.user.User;

/** Published after a ticket notification decision has been made. */
public record TicketNotificationCreatedEvent(
        User recipient,
        String type,
        Long ticketId,
        String ticketCode,
        String message,
        boolean emailEnabled) {
}
