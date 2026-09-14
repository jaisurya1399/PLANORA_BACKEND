package com.projectmanagement.app.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRuleRepository extends JpaRepository<WorkflowRule, Long> {
    List<WorkflowRule> findAllByOrderByIdAsc();

    List<WorkflowRule> findByProjectIdAndActiveTrueOrderByIdAsc(Long projectId);

    List<WorkflowRule> findByProjectIdAndTicketTypeIdAndFromStatusIdAndToStatusIdAndActiveTrue(Long projectId,
            Long ticketTypeId, Long fromStatusId, Long toStatusId);
}
