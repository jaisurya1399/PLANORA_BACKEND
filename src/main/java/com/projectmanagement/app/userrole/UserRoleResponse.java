package com.projectmanagement.app.userrole;

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
public class UserRoleResponse {

    private Long userId;
    private String userName;
    private String userEmail;

    private Long roleId;
    private String roleName;
    private String guardName;

    private String modelType;
}