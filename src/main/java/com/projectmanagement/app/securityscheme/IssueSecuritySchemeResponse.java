package com.projectmanagement.app.securityscheme;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IssueSecuritySchemeResponse {
    Long id, projectId;
    String projectName, name;
    IssueSecurityLevel defaultLevel;
    Boolean active;
}
