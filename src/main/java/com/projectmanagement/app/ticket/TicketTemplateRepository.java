package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTemplateRepository extends JpaRepository<TicketTemplate, Long> {
    List<TicketTemplate> findAllByOrderByNameAsc();

    List<TicketTemplate> findByProjectIdAndActiveTrueOrderByNameAsc(Long projectId);
}
