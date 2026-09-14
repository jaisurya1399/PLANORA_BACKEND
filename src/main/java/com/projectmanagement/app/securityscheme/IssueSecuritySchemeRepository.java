package com.projectmanagement.app.securityscheme;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueSecuritySchemeRepository extends JpaRepository<IssueSecurityScheme, Long> {
    Optional<IssueSecurityScheme> findByProjectId(Long projectId);
}
