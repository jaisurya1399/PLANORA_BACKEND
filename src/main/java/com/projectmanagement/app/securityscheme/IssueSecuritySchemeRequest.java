package com.projectmanagement.app.securityscheme;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class IssueSecuritySchemeRequest {
    @NotNull
    @Positive
    Long projectId;
    @NotBlank
    String name;
    @NotNull
    IssueSecurityLevel defaultLevel;
    Boolean active;
}
