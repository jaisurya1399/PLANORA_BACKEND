package com.projectmanagement.app.risk;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRiskRepository extends JpaRepository<ProjectRisk, Long> {
    List<ProjectRisk> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    long countByProjectIdAndStatusIgnoreCase(Long projectId, String status);
}
