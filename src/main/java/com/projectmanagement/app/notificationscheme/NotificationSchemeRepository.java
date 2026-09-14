package com.projectmanagement.app.notificationscheme;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSchemeRepository extends JpaRepository<NotificationScheme, Long> {
    Optional<NotificationScheme> findByProjectId(Long projectId);
}
