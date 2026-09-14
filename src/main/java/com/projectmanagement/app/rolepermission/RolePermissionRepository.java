package com.projectmanagement.app.rolepermission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, RolePermissionId> {

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByPermissionId(Long permissionId);

    Optional<RolePermission> findByRoleIdAndPermissionId(
            Long roleId,
            Long permissionId);

    boolean existsByRoleIdAndPermissionId(
            Long roleId,
            Long permissionId);

    void deleteByRoleIdAndPermissionId(
            Long roleId,
            Long permissionId);

    void deleteByRoleId(Long roleId);

    void deleteByPermissionId(Long permissionId);
}