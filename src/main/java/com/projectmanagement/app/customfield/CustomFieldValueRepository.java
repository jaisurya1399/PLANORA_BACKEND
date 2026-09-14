package com.projectmanagement.app.customfield;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {
    List<CustomFieldValue> findByTicketId(Long ticketId);

    void deleteByTicketId(Long ticketId);
}
