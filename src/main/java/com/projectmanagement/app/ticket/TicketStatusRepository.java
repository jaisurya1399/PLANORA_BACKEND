package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketStatusRepository
        extends JpaRepository<TicketStatus, Long> {

    List<TicketStatus> findByDeletedAtIsNull();

    List<TicketStatus> findByProjectId(Long projectId);

    List<TicketStatus> findByProjectIdAndDeletedAtIsNull(
            Long projectId);

    List<TicketStatus> findByProjectIdIsNull();

    List<TicketStatus> findByProjectIdIsNullAndDeletedAtIsNull();

    List<TicketStatus> findByIsDefaultTrue();

    List<TicketStatus> findByProjectIdAndIsDefaultTrue(
            Long projectId);

    Optional<TicketStatus> findByName(String name);

    Optional<TicketStatus> findByNameAndDeletedAtIsNull(
            String name);

    Optional<TicketStatus> findByProjectIdAndName(
            Long projectId,
            String name);

    Optional<TicketStatus> findByProjectIdAndNameAndDeletedAtIsNull(
            Long projectId,
            String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(
            String name,
            Long id);

    boolean existsByProjectIdAndName(
            Long projectId,
            String name);

    boolean existsByProjectIdAndNameAndIdNot(
            Long projectId,
            String name,
            Long id);

    List<TicketStatus> findByColor(String color);

    List<TicketStatus> findByProjectIdOrderByOrderAsc(
            Long projectId);

    List<TicketStatus> findByProjectIdAndDeletedAtIsNullOrderByOrderAsc(
            Long projectId);
}