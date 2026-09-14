package com.projectmanagement.app.permission;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    Optional<Permission> findByNameAndGuardName(
            String name,
            String guardName);

    boolean existsByNameAndGuardName(
            String name,
            String guardName);

    boolean existsByNameAndGuardNameAndIdNot(
            String name,
            String guardName,
            Long id);
}