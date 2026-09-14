package com.projectmanagement.app.automation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {
    List<AutomationRule> findByProjectId(Long projectId);

    List<AutomationRule> findByEnabledTrueAndTriggerEvent(String triggerEvent);
}
