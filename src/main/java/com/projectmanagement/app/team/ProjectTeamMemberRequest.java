package com.projectmanagement.app.team;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProjectTeamMemberRequest {
    @NotNull
    @Positive
    Long userId;
}
