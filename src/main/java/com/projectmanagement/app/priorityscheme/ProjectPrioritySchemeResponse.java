package com.projectmanagement.app.priorityscheme;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectPrioritySchemeResponse {
    Long id, projectId;
    String projectName, name;
    List<Long> priorityIds;
    Boolean active;
}
