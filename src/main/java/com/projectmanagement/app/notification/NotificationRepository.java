package com.projectmanagement.app.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findByNotifiableTypeAndNotifiableId(
            String notifiableType,
            Long notifiableId);

    List<Notification> findByNotifiableTypeAndNotifiableIdOrderByCreatedAtDesc(
            String notifiableType,
            Long notifiableId);

    List<Notification> findByNotifiableTypeAndNotifiableIdAndReadAtIsNull(
            String notifiableType,
            Long notifiableId);

    List<Notification> findByNotifiableTypeAndNotifiableIdAndReadAtIsNotNull(
            String notifiableType,
            Long notifiableId);

    long countByNotifiableTypeAndNotifiableId(
            String notifiableType,
            Long notifiableId);

    long countByNotifiableTypeAndNotifiableIdAndReadAtIsNull(
            String notifiableType,
            Long notifiableId);

    void deleteByNotifiableTypeAndNotifiableId(
            String notifiableType,
            Long notifiableId);
}