package com.projectmanagement.app.project;

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
public class ProjectMembershipResponse {
    private Long projectId;
    private String projectName;
    private String role;
    private String responsibilityRole;
}
