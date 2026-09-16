package com.projectmanagement.app.notification.fcm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.projectmanagement.app.notification.TicketNotificationCreatedEvent;

@Service
@ConditionalOnProperty(name = "app.notification.fcm.enabled", havingValue = "true")
public class FcmService {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    private final FcmDeviceTokenRepository tokenRepository;

    public FcmService(FcmDeviceTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Async("notificationTaskExecutor")
    public void send(TicketNotificationCreatedEvent event) {
        List<FcmDeviceToken> devices = tokenRepository.findByUserId(event.recipient().getId());
        if (devices.isEmpty())
            return;

        List<String> tokens = devices.stream().map(FcmDeviceToken::getToken).toList();

        Map<String, String> data = new HashMap<>();
        data.put("type", event.type());
        data.put("ticketId", String.valueOf(event.ticketId()));
        data.put("ticketCode", event.ticketCode());
        data.put("message", event.message());

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle("[" + event.ticketCode() + "] " + event.type().replace('_', ' '))
                        .setBody(event.message())
                        .build())
                .putAllData(data)
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            removeInvalidTokens(devices, response);
            log.debug("FCM notification sent to user {}: success={}, failure={}",
                    event.recipient().getId(), response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException ex) {
            log.warn("FCM delivery failed for user {}", event.recipient().getId(), ex);
        }
    }

    private void removeInvalidTokens(List<FcmDeviceToken> devices, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful())
                continue;

            FirebaseMessagingException ex = sendResponse.getException();
            if (ex == null)
                continue;

            var code = ex.getMessagingErrorCode();
            if (code == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED
                    || code == com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT) {
                tokenRepository.deleteByToken(devices.get(i).getToken());
            }
        }
    }
}
