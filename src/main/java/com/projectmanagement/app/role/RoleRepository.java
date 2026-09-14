package com.projectmanagement.app.role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Optional<Role> findByNameAndGuardName(
            String name,
            String guardName);

    boolean existsByName(String name);

    boolean existsByNameAndGuardName(
            String name,
            String guardName);

    List<Role> findByGuardName(String guardName);
}