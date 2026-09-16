package com.projectmanagement.app.notification.fcm;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectmanagement.app.auth.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications/fcm")
@RequiredArgsConstructor
public class FcmDeviceTokenController {

    private final FcmDeviceTokenRepository repository;
    private final CurrentUserService currentUserService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody FcmTokenRequest request) {
        String token = request.token() == null ? "" : request.token().trim();
        if (token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "FCM token is required"));
        }

        var user = currentUserService.getCurrentUser();
        var existing = repository.findByToken(token).orElse(null);

        if (existing != null) {
            existing.setUser(user);
            existing.setPlatform(request.platform());
            repository.save(existing);
        } else {
            repository.save(FcmDeviceToken.builder()
                    .user(user)
                    .token(token)
                    .platform(request.platform())
                    .build());
        }

        return ResponseEntity.ok(Map.of("registered", true));
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<?> unregister(@RequestBody FcmTokenRequest request) {
        if (request.token() != null && !request.token().isBlank()) {
            repository.deleteByToken(request.token().trim());
        }
        return ResponseEntity.noContent().build();
    }

    public record FcmTokenRequest(String token, String platform) {
    }
}
