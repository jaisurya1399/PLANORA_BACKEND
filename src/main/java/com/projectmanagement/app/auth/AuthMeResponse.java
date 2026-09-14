package com.projectmanagement.app.auth;

import java.util.List;

import com.projectmanagement.app.project.ProjectMembershipResponse;

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
public class AuthMeResponse {

    private Long userId;

    private String name;

    private String email;

    private String role;

    private List<String> permissions;

    private List<ProjectMembershipResponse> projectMemberships;

    private boolean mfaEnabled;
}