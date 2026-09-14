package com.projectmanagement.app.securityscheme;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectPermissionSchemeResponse {
    Long id, projectId;
    String projectName, name, grantsJson;
    Boolean active;
}
