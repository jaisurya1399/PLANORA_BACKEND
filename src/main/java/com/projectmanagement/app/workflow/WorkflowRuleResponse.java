package com.projectmanagement.app.workflow;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowRuleResponse {
    Long id, projectId, ticketTypeId, fromStatusId, toStatusId;
    String projectName, ticketTypeName, fromStatusName, toStatusName, requiredPermission, conditionJson, validatorJson,
            postFunctionJson;
    Boolean active;
}
