package com.projectmanagement.app.rolepermission;

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
public class RolePermissionResponse {

    private Long roleId;
    private String roleName;
    private String guardName;

    private Long permissionId;
    private String permissionName;
    private String permissionGuardName;
}