package com.projectmanagement.app.notification;

import com.projectmanagement.app.user.User;

/** Published after a ticket notification has been created. */
public record TicketNotificationCreatedEvent(
        User recipient,
        String type,
        Long ticketId,
        String ticketCode,
        String message) {
}
