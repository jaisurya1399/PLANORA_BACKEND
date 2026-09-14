package com.projectmanagement.app.notification;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
@RestController @RequestMapping("/api/notification-preferences") @RequiredArgsConstructor @Validated
public class NotificationPreferenceController {
 private final NotificationPreferenceService service;
 @GetMapping("/user/{userId}") @PreAuthorize("hasAuthority('notification.view') or hasRole('ADMIN')") public ResponseEntity<List<NotificationPreferenceResponse>> get(@PathVariable @Positive Long userId){return ResponseEntity.ok(service.get(userId));}
 @PutMapping("/user/{userId}") @PreAuthorize("hasAuthority('notification.update') or hasRole('ADMIN')") public ResponseEntity<NotificationPreferenceResponse> save(@PathVariable @Positive Long userId,@Valid @RequestBody NotificationPreferenceRequest request){return ResponseEntity.ok(service.save(userId,request));}
}
