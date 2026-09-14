package com.projectmanagement.app.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<AuditEvent> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
