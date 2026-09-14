package com.projectmanagement.app.notification;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/web-push")
@ConditionalOnProperty(name = "app.notification.web-push.enabled", havingValue = "true")
public class WebPushConfigurationController {
    private final String vapidPublicKey;

    public WebPushConfigurationController(
            @Value("${app.notification.web-push.vapid-public-key}") String vapidPublicKey) {
        this.vapidPublicKey = vapidPublicKey;
    }

    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> vapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }
}
