package com.projectmanagement.app.notification;

import java.security.Security;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;

@Component
@ConditionalOnProperty(name = "app.notification.web-push.enabled", havingValue = "true")
public class WebPushTicketNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(
            WebPushTicketNotificationListener.class);

    private final WebPushSubscriptionRepository subscriptions;
    private final ObjectMapper objectMapper;

    private final String vapidPublicKey;
    private final String vapidPrivateKey;
    private final String vapidSubject;

    public WebPushTicketNotificationListener(
            WebPushSubscriptionRepository subscriptions,
            ObjectMapper objectMapper,
            @Value("${app.notification.web-push.vapid-public-key}") String vapidPublicKey,
            @Value("${app.notification.web-push.vapid-private-key}") String vapidPrivateKey,
            @Value("${app.notification.web-push.vapid-subject}") String vapidSubject) {

        this.subscriptions = subscriptions;
        this.objectMapper = objectMapper;
        this.vapidPublicKey = vapidPublicKey;
        this.vapidPrivateKey = vapidPrivateKey;
        this.vapidSubject = vapidSubject;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void deliver(
            TicketNotificationCreatedEvent event) {

        /*
         * Web Push encryption requires Bouncy Castle.
         *
         * Register it only once.
         */
        if (Security.getProvider(
                BouncyCastleProvider.PROVIDER_NAME) == null) {

            Security.addProvider(
                    new BouncyCastleProvider());
        }

        subscriptions
                .findByUserId(
                        event.recipient().getId())
                .forEach(subscription -> send(subscription, event));
    }

    private void send(
            WebPushSubscription stored,
            TicketNotificationCreatedEvent event) {

        try {

            // -----------------------------------------------------
            // Build browser push subscription
            // -----------------------------------------------------

            String subscriptionJson = objectMapper.writeValueAsString(
                    Map.of(
                            "endpoint",
                            stored.getEndpoint(),
                            "keys",
                            Map.of(
                                    "p256dh",
                                    stored.getP256dh(),
                                    "auth",
                                    stored.getAuth())));

            Subscription subscription = objectMapper.readValue(
                    subscriptionJson,
                    Subscription.class);

            // -----------------------------------------------------
            // Build notification payload
            // -----------------------------------------------------

            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "title",
                            "["
                                    + event.ticketCode()
                                    + "] "
                                    + event.type()
                                            .replace('_', ' '),

                            "body",
                            event.message(),

                            "ticketId",
                            event.ticketId(),

                            "ticketCode",
                            event.ticketCode()));

            // -----------------------------------------------------
            // Configure Web Push / VAPID
            // -----------------------------------------------------

            PushService pushService = new PushService()
                    .setPublicKey(
                            vapidPublicKey)
                    .setPrivateKey(
                            vapidPrivateKey)
                    .setSubject(
                            vapidSubject);

            // -----------------------------------------------------
            // Send notification
            //
            // web-push 5.1.2 expects String payload here.
            // Do NOT convert payload to byte[].
            // -----------------------------------------------------

            pushService.send(
                    new Notification(
                            subscription,
                            payload));

        } catch (Exception exception) {

            log.warn(
                    "Unable to deliver browser push notification " +
                            "for subscription {}",
                    stored.getId(),
                    exception);
        }
    }
}