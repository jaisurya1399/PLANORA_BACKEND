package com.projectmanagement.app.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projectmanagement.app.auth.CurrentUserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

        private final NotificationService notificationService;
        private final CurrentUserService currentUserService;

        @GetMapping
        @PreAuthorize("hasAuthority('notification.view') or hasRole('ADMIN')")
        public ResponseEntity<List<NotificationResponse>> getAll() {

                return ResponseEntity.ok(
                                notificationService.getAll());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('notification.view') or hasRole('ADMIN')")
        public ResponseEntity<NotificationResponse> getById(
                        @PathVariable UUID id) {

                return ResponseEntity.ok(
                                notificationService.getById(id));
        }

        @GetMapping("/user")
        public ResponseEntity<List<NotificationResponse>> getByNotifiable(
                        @RequestParam String notifiableType,
                        @RequestParam @Positive Long notifiableId) {
                requireOwnUser(notifiableId);
                return ResponseEntity.ok(
                                notificationService.getByNotifiable(
                                                notifiableType,
                                                notifiableId));
        }

        @GetMapping("/user/unread")
        public ResponseEntity<List<NotificationResponse>> getUnread(
                        @RequestParam String notifiableType,
                        @RequestParam @Positive Long notifiableId) {
                requireOwnUser(notifiableId);
                return ResponseEntity.ok(
                                notificationService.getUnread(
                                                notifiableType,
                                                notifiableId));
        }

        @GetMapping("/user/read")
        public ResponseEntity<List<NotificationResponse>> getRead(
                        @RequestParam String notifiableType,
                        @RequestParam @Positive Long notifiableId) {
                requireOwnUser(notifiableId);
                return ResponseEntity.ok(
                                notificationService.getRead(
                                                notifiableType,
                                                notifiableId));
        }

        @GetMapping("/user/count")
        public ResponseEntity<Long> count(
                        @RequestParam String notifiableType,
                        @RequestParam @Positive Long notifiableId) {
                requireOwnUser(notifiableId);
                return ResponseEntity.ok(
                                notificationService.count(
                                                notifiableType,
                                                notifiableId));
        }

        @GetMapping("/user/unread-count")
        public ResponseEntity<Long> countUnread(
                        @RequestParam String notifiableType,
                        @RequestParam @Positive Long notifiableId) {
                requireOwnUser(notifiableId);
                return ResponseEntity.ok(
                                notificationService.countUnread(
                                                notifiableType,
                                                notifiableId));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('notification.create') or hasRole('ADMIN')")
        public ResponseEntity<NotificationResponse> create(
                        @Valid @RequestBody NotificationRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(notificationService.create(request));
        }

        @PutMapping("/{id}/read")
        public ResponseEntity<NotificationResponse> markAsRead(
                        @PathVariable UUID id) {
                requireOwnNotification(id);
                return ResponseEntity.ok(
                                notificationService.markAsRead(id));
        }

        @PutMapping("/{id}/unread")
        public ResponseEntity<NotificationResponse> markAsUnread(
                        @PathVariable UUID id) {
                requireOwnNotification(id);
                return ResponseEntity.ok(
                                notificationService.markAsUnread(id));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @PathVariable UUID id) {
                requireOwnNotification(id);
                notificationService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/user")
        public ResponseEntity<Void> deleteByNotifiable(
                        @RequestParam String notifiableType,
                        @RequestParam @Positive Long notifiableId) {
                requireOwnUser(notifiableId);
                notificationService.deleteByNotifiable(
                                notifiableType,
                                notifiableId);

                return ResponseEntity.noContent().build();
        }

        private void requireOwnUser(Long userId) {
                Long currentId = currentUserService.getCurrentUserId();
                boolean admin = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication() != null
                                && org.springframework.security.core.context.SecurityContextHolder
                                                .getContext().getAuthentication().getAuthorities().stream()
                                                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                boolean canViewAll = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication() != null
                                && org.springframework.security.core.context.SecurityContextHolder
                                                .getContext().getAuthentication().getAuthorities().stream()
                                                .anyMatch(a -> "notification.view".equalsIgnoreCase(a.getAuthority()));
                if (!admin && !canViewAll && !java.util.Objects.equals(currentId, userId)) {
                        throw new org.springframework.web.server.ResponseStatusException(
                                        org.springframework.http.HttpStatus.FORBIDDEN,
                                        "You can only access your own notifications");
                }
        }

        private void requireOwnNotification(UUID id) {
                NotificationResponse notification = notificationService.getById(id);
                requireOwnUser(notification.getNotifiableId());
        }

}