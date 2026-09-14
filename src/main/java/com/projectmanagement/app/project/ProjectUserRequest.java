package com.projectmanagement.app.project;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUserRequest {

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long projectId;

    @NotNull
    private ProjectRole role;

    /** Required only when role == MEMBER. */
    private MemberResponsibility responsibilityRole;

    private Boolean availabilitySelfUpdateOpen;
}
