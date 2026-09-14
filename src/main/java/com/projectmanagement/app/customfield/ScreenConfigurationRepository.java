package com.projectmanagement.app.customfield;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreenConfigurationRepository extends JpaRepository<ScreenConfiguration, Long> {
    List<ScreenConfiguration> findAllByOrderByNameAsc();

    List<ScreenConfiguration> findByProjectIdAndTicketTypeIdAndActiveTrue(Long projectId, Long ticketTypeId);

    List<ScreenConfiguration> findByProjectIdIsNullAndTicketTypeIdAndActiveTrue(Long ticketTypeId);

    List<ScreenConfiguration> findByProjectIdAndTicketTypeIdIsNullAndActiveTrue(Long projectId);

    List<ScreenConfiguration> findByProjectIdIsNullAndTicketTypeIdIsNullAndActiveTrue();
}
