package com.projectmanagement.app.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/web-push-subscriptions")
public class WebPushSubscriptionController {
    private final WebPushSubscriptionService service;
    public WebPushSubscriptionController(WebPushSubscriptionService service) { this.service = service; }
    @PostMapping public ResponseEntity<Void> subscribe(@Valid @RequestBody WebPushSubscriptionRequest request) { service.upsert(request); return ResponseEntity.noContent().build(); }
    @DeleteMapping public ResponseEntity<Void> unsubscribe(@RequestParam String endpoint) { service.remove(endpoint); return ResponseEntity.noContent().build(); }
}
