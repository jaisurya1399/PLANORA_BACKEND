package com.projectmanagement.app.rolepermission;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/role-permissions")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(
            RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    // ============================================================
    // Get all role-permission mappings
    // ============================================================

    @GetMapping
    @PreAuthorize("hasAuthority('role_permission.view') or hasRole('ADMIN')")
    public ResponseEntity<List<RolePermissionResponse>> getAllRolePermissions() {

        List<RolePermissionResponse> response = rolePermissionService
                .getAllRolePermissions()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // Get permissions assigned to a role
    // ============================================================

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('role_permission.view') or hasRole('ADMIN')")
    public ResponseEntity<List<RolePermissionResponse>> getByRoleId(
            @PathVariable Long roleId) {

        List<RolePermissionResponse> response = rolePermissionService
                .getByRoleId(roleId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // Get roles assigned to a permission
    // ============================================================

    @GetMapping("/permission/{permissionId}")
    @PreAuthorize("hasAuthority('role_permission.view') or hasRole('ADMIN')")
    public ResponseEntity<List<RolePermissionResponse>> getByPermissionId(
            @PathVariable Long permissionId) {

        List<RolePermissionResponse> response = rolePermissionService
                .getByPermissionId(permissionId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // Get specific role-permission mapping
    // ============================================================

    @GetMapping("/role/{roleId}/permission/{permissionId}")
    @PreAuthorize("hasAuthority('role_permission.view') or hasRole('ADMIN')")
    public ResponseEntity<RolePermissionResponse> getRolePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        RolePermission rolePermission = rolePermissionService.getRolePermission(
                roleId,
                permissionId);

        return ResponseEntity.ok(
                toResponse(rolePermission));
    }

    // ============================================================
    // Check whether permission is assigned to role
    // ============================================================

    @GetMapping("/role/{roleId}/permission/{permissionId}/exists")
    @PreAuthorize("hasAuthority('role_permission.view') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> exists(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        boolean exists = rolePermissionService
                .existsByRoleAndPermission(
                        roleId,
                        permissionId);

        return ResponseEntity.ok(exists);
    }

    // ============================================================
    // Assign permission to role
    // ============================================================

    @PostMapping("/role/{roleId}/permission/{permissionId}")
    @PreAuthorize("hasAuthority('role_permission.assign') or hasRole('ADMIN')")
    public ResponseEntity<RolePermissionResponse> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        RolePermission rolePermission = rolePermissionService.assignPermission(
                roleId,
                permissionId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(rolePermission));
    }

    // ============================================================
    // Remove permission from role
    // ============================================================

    @DeleteMapping("/role/{roleId}/permission/{permissionId}")
    @PreAuthorize("hasAuthority('role_permission.remove') or hasRole('ADMIN')")
    public ResponseEntity<Void> removePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        rolePermissionService.removePermission(
                roleId,
                permissionId);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // Remove all permissions from role
    // ============================================================

    @DeleteMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('role_permission.remove') or hasRole('ADMIN')")
    public ResponseEntity<Void> removeAllPermissionsFromRole(
            @PathVariable Long roleId) {

        rolePermissionService
                .removeAllPermissionsFromRole(roleId);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // Remove permission from all roles
    // ============================================================

    @DeleteMapping("/permission/{permissionId}")
    @PreAuthorize("hasAuthority('role_permission.remove') or hasRole('ADMIN')")
    public ResponseEntity<Void> removePermissionFromAllRoles(
            @PathVariable Long permissionId) {

        rolePermissionService
                .removePermissionFromAllRoles(permissionId);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // Entity -> DTO
    // ============================================================

    private RolePermissionResponse toResponse(
            RolePermission rolePermission) {

        if (rolePermission == null) {
            return null;
        }

        return RolePermissionResponse.builder()
                .roleId(
                        rolePermission.getRole() != null
                                ? rolePermission
                                        .getRole()
                                        .getId()
                                : null)
                .roleName(
                        rolePermission.getRole() != null
                                ? rolePermission
                                        .getRole()
                                        .getName()
                                : null)
                .guardName(
                        rolePermission.getRole() != null
                                ? rolePermission
                                        .getRole()
                                        .getGuardName()
                                : null)
                .permissionId(
                        rolePermission.getPermission() != null
                                ? rolePermission
                                        .getPermission()
                                        .getId()
                                : null)
                .permissionName(
                        rolePermission.getPermission() != null
                                ? rolePermission
                                        .getPermission()
                                        .getName()
                                : null)
                .permissionGuardName(
                        rolePermission.getPermission() != null
                                ? rolePermission
                                        .getPermission()
                                        .getGuardName()
                                : null)
                .build();
    }
}