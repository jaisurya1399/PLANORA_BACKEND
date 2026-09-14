package com.projectmanagement.app.workflow;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowSchemeResponse {
    Long id, projectId;
    String projectName, name;
    List<Long> ruleIds;
    Boolean active;
}
