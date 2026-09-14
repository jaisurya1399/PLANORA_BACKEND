package com.projectmanagement.app.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectTemplateRequest {
    @NotBlank
    private String name;

    private String description;

    private Long sourceProjectId;
}
