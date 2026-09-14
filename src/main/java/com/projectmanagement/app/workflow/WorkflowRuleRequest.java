package com.projectmanagement.app.workflow;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WorkflowRuleRequest {
    @Positive
    Long projectId;
    @Positive
    Long ticketTypeId;
    @NotNull
    @Positive
    Long fromStatusId;
    @NotNull
    @Positive
    Long toStatusId;
    String requiredPermission, conditionJson, validatorJson, postFunctionJson;
    Boolean active;
}
