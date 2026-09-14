package com.projectmanagement.app.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowSchemeRepository extends JpaRepository<WorkflowScheme, Long> {
    List<WorkflowScheme> findByProjectIdAndActiveTrueOrderByNameAsc(Long projectId);
}
