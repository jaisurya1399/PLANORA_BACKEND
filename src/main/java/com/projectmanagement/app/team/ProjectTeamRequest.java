package com.projectmanagement.app.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProjectTeamRequest {
    @NotNull
    @Positive
    Long projectId;
    @NotBlank
    String name;
    String description;
    Boolean active;
}
