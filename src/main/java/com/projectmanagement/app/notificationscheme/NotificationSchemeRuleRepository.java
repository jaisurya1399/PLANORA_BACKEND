package com.projectmanagement.app.notificationscheme;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSchemeRuleRepository extends JpaRepository<NotificationSchemeRule, Long> {
    List<NotificationSchemeRule> findBySchemeId(Long schemeId);
}
