package com.projectmanagement.app.priorityscheme;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPrioritySchemeRepository extends JpaRepository<ProjectPriorityScheme, Long> {
    Optional<ProjectPriorityScheme> findByProjectId(Long projectId);
}
