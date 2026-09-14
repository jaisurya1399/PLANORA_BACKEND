package com.projectmanagement.app.usergroup;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    Optional<UserGroup> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
