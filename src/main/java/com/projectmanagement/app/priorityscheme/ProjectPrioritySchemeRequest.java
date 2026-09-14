package com.projectmanagement.app.priorityscheme;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProjectPrioritySchemeRequest {
    @NotNull
    @Positive
    Long projectId;
    @NotBlank
    String name;
    @NotEmpty
    List<@NotNull @Positive Long> priorityIds;
    Boolean active;
}
