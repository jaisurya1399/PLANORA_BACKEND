package com.projectmanagement.app.workflow;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WorkflowSchemeRequest {
    @NotNull
    @Positive
    Long projectId;
    @NotBlank
    String name;
    @NotNull
    List<@NotNull @Positive Long> ruleIds;
    Boolean active;
}
