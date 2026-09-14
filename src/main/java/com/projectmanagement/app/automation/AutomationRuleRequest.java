package com.projectmanagement.app.automation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRuleRequest {
    private Long projectId;
    private String name;
    private String triggerEvent;
    private String conditionExpression;
    private String actionType;
    private String actionValue;
    private Boolean enabled;
}
