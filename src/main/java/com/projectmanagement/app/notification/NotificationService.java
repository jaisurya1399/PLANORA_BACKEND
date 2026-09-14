package com.projectmanagement.app.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

        private final NotificationRepository notificationRepository;

        @Transactional(readOnly = true)
        public List<NotificationResponse> getAll() {

                return notificationRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public NotificationResponse getById(UUID id) {

                Notification notification = notificationRepository
                                .findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Notification not found"));

                return toResponse(notification);
        }

        @Transactional(readOnly = true)
        public List<NotificationResponse> getByNotifiable(
                        String notifiableType,
                        Long notifiableId) {

                return notificationRepository
                                .findByNotifiableTypeAndNotifiableIdOrderByCreatedAtDesc(
                                                canonicalType(notifiableType),
                                                notifiableId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<NotificationResponse> getUnread(
                        String notifiableType,
                        Long notifiableId) {

                return notificationRepository
                                .findByNotifiableTypeAndNotifiableIdAndReadAtIsNull(
                                                canonicalType(notifiableType),
                                                notifiableId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<NotificationResponse> getRead(
                        String notifiableType,
                        Long notifiableId) {

                return notificationRepository
                                .findByNotifiableTypeAndNotifiableIdAndReadAtIsNotNull(
                                                canonicalType(notifiableType),
                                                notifiableId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        public NotificationResponse create(
                        NotificationRequest request) {

                Notification notification = Notification.builder()
                                .type(request.getType())
                                .notifiableType(canonicalType(request.getNotifiableType()))
                                .notifiableId(request.getNotifiableId())
                                .data(request.getData())
                                .build();

                return toResponse(
                                notificationRepository.save(notification));
        }

        public NotificationResponse markAsRead(UUID id) {

                Notification notification = getEntity(id);

                notification.setReadAt(LocalDateTime.now());

                return toResponse(
                                notificationRepository.save(notification));
        }

        public NotificationResponse markAsUnread(UUID id) {

                Notification notification = getEntity(id);

                notification.setReadAt(null);

                return toResponse(
                                notificationRepository.save(notification));
        }

        public void delete(UUID id) {

                Notification notification = getEntity(id);

                notificationRepository.delete(notification);
        }

        public void deleteByNotifiable(
                        String notifiableType,
                        Long notifiableId) {

                notificationRepository
                                .deleteByNotifiableTypeAndNotifiableId(
                                                notifiableType,
                                                notifiableId);
        }

        @Transactional(readOnly = true)
        public long count(
                        String notifiableType,
                        Long notifiableId) {

                return notificationRepository
                                .countByNotifiableTypeAndNotifiableId(
                                                canonicalType(notifiableType),
                                                notifiableId);
        }

        @Transactional(readOnly = true)
        public long countUnread(
                        String notifiableType,
                        Long notifiableId) {

                return notificationRepository
                                .countByNotifiableTypeAndNotifiableIdAndReadAtIsNull(
                                                canonicalType(notifiableType),
                                                notifiableId);
        }

        private String canonicalType(String value) {
                return value == null ? "USER" : value.trim().toUpperCase();
        }

        private Notification getEntity(UUID id) {

                return notificationRepository
                                .findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Notification not found"));
        }

        private NotificationResponse toResponse(
                        Notification notification) {

                return NotificationResponse.builder()
                                .id(notification.getId())
                                .type(notification.getType())
                                .notifiableType(notification.getNotifiableType())
                                .notifiableId(notification.getNotifiableId())
                                .data(notification.getData())
                                .readAt(notification.getReadAt())
                                .createdAt(notification.getCreatedAt())
                                .updatedAt(notification.getUpdatedAt())
                                .build();
        }
}