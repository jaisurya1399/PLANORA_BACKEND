package com.projectmanagement.app.automation;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AutomationRuleResponse {
    Long id;
    Long projectId;
    String projectName;
    String name;
    String triggerEvent;
    String conditionExpression;
    String actionType;
    String actionValue;
    boolean enabled;
    LocalDateTime lastRunAt;
}
