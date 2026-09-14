package com.projectmanagement.app.rolepermission;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.permission.Permission;
import com.projectmanagement.app.permission.PermissionRepository;
import com.projectmanagement.app.role.Role;
import com.projectmanagement.app.role.RoleRepository;

@Service
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionService(
            RolePermissionRepository rolePermissionRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    // ============================================================
    // Get all role-permission mappings
    // ============================================================

    @Transactional(readOnly = true)
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }

    // ============================================================
    // Get permissions assigned to a role
    // ============================================================

    @Transactional(readOnly = true)
    public List<RolePermission> getByRoleId(Long roleId) {

        getRole(roleId);

        return rolePermissionRepository.findByRoleId(roleId);
    }

    // ============================================================
    // Get roles assigned to a permission
    // ============================================================

    @Transactional(readOnly = true)
    public List<RolePermission> getByPermissionId(
            Long permissionId) {

        getPermission(permissionId);

        return rolePermissionRepository
                .findByPermissionId(permissionId);
    }

    // ============================================================
    // Get specific role-permission mapping
    // ============================================================

    @Transactional(readOnly = true)
    public RolePermission getRolePermission(
            Long roleId,
            Long permissionId) {

        validateRoleId(roleId);
        validatePermissionId(permissionId);

        return rolePermissionRepository
                .findByRoleIdAndPermissionId(
                        roleId,
                        permissionId)
                .orElseThrow(() -> new RuntimeException(
                        "Permission is not assigned to role"));
    }

    // ============================================================
    // Check whether mapping exists
    // ============================================================

    @Transactional(readOnly = true)
    public boolean existsByRoleAndPermission(
            Long roleId,
            Long permissionId) {

        validateRoleId(roleId);
        validatePermissionId(permissionId);

        return rolePermissionRepository
                .existsByRoleIdAndPermissionId(
                        roleId,
                        permissionId);
    }

    // ============================================================
    // Assign permission to role
    // ============================================================

    @Transactional
    public RolePermission assignPermission(
            Long roleId,
            Long permissionId) {

        Role role = getRole(roleId);
        Permission permission = getPermission(permissionId);

        // --------------------------------------------------------
        // Prevent duplicate composite-key mapping
        // --------------------------------------------------------

        if (rolePermissionRepository
                .existsByRoleIdAndPermissionId(
                        roleId,
                        permissionId)) {

            throw new RuntimeException(
                    "Permission is already assigned to role");
        }

        // --------------------------------------------------------
        // Create mapping
        //
        // Primary key:
        // (permission_id, role_id)
        // --------------------------------------------------------

        RolePermission rolePermission = RolePermission.builder()
                .role(role)
                .permission(permission)
                .build();

        return rolePermissionRepository.save(
                rolePermission);
    }

    // ============================================================
    // Remove permission from role
    // ============================================================

    @Transactional
    public void removePermission(
            Long roleId,
            Long permissionId) {

        validateRoleId(roleId);
        validatePermissionId(permissionId);

        if (!rolePermissionRepository
                .existsByRoleIdAndPermissionId(
                        roleId,
                        permissionId)) {

            throw new RuntimeException(
                    "Permission is not assigned to role");
        }

        rolePermissionRepository
                .deleteByRoleIdAndPermissionId(
                        roleId,
                        permissionId);
    }

    // ============================================================
    // Remove all permissions from role
    // ============================================================

    @Transactional
    public void removeAllPermissionsFromRole(
            Long roleId) {

        getRole(roleId);

        rolePermissionRepository.deleteByRoleId(roleId);
    }

    // ============================================================
    // Remove permission from every role
    // ============================================================

    @Transactional
    public void removePermissionFromAllRoles(
            Long permissionId) {

        getPermission(permissionId);

        rolePermissionRepository
                .deleteByPermissionId(permissionId);
    }

    // ============================================================
    // Validate Role
    // ============================================================

    private Role getRole(Long roleId) {

        validateRoleId(roleId);

        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found with id: " + roleId));
    }

    // ============================================================
    // Validate Permission
    // ============================================================

    private Permission getPermission(Long permissionId) {

        validatePermissionId(permissionId);

        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException(
                        "Permission not found with id: "
                                + permissionId));
    }

    // ============================================================
    // ID validation
    // ============================================================

    private void validateRoleId(Long roleId) {

        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException(
                    "Role ID must be greater than zero");
        }
    }

    private void validatePermissionId(Long permissionId) {

        if (permissionId == null || permissionId <= 0) {
            throw new IllegalArgumentException(
                    "Permission ID must be greater than zero");
        }
    }
}