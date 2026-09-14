package com.projectmanagement.app.customfield;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldConfigurationRepository extends JpaRepository<FieldConfiguration, Long> {
    List<FieldConfiguration> findAllByOrderByDisplayOrderAscIdAsc();

    List<FieldConfiguration> findByProjectIdAndTicketTypeIdOrderByDisplayOrderAscIdAsc(Long projectId,
            Long ticketTypeId);

    List<FieldConfiguration> findByProjectIdIsNullAndTicketTypeIdOrderByDisplayOrderAscIdAsc(Long ticketTypeId);

    List<FieldConfiguration> findByProjectIdAndTicketTypeIdIsNullOrderByDisplayOrderAscIdAsc(Long projectId);

    List<FieldConfiguration> findByProjectIdIsNullAndTicketTypeIdIsNullOrderByDisplayOrderAscIdAsc();

    boolean existsByFieldIdAndProjectIdAndTicketTypeId(Long fieldId, Long projectId, Long ticketTypeId);
}
