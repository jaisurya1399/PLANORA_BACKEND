package com.projectmanagement.app.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectTemplateCreateProjectRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String ticketPrefix;

    private Long ownerId;
}
