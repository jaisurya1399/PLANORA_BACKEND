package com.projectmanagement.app.sla;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    List<SlaPolicy> findByProjectId(Long projectId);
}
