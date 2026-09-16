package com.projectmanagement.app.notification.fcm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.projectmanagement.app.notification.TicketNotificationCreatedEvent;

@Component
@ConditionalOnProperty(name = "app.notification.fcm.enabled", havingValue = "true")
public class FcmTicketNotificationListener {

    private final FcmService fcmService;

    public FcmTicketNotificationListener(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void deliver(TicketNotificationCreatedEvent event) {
        if (!event.pushEnabled())
            return;
        fcmService.send(event);
    }
}
