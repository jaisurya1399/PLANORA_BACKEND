package com.projectmanagement.app.securityscheme;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPermissionSchemeRepository extends JpaRepository<ProjectPermissionScheme, Long> {
    Optional<ProjectPermissionScheme> findByProjectId(Long projectId);
}
