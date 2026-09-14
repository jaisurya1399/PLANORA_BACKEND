package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketPriorityRepository
        extends JpaRepository<TicketPriority, Long> {

    List<TicketPriority> findByDeletedAtIsNullOrderByDisplayOrderAscIdAsc();

    List<TicketPriority> findByDeletedAtIsNull();

    List<TicketPriority> findByIsDefaultTrue();

    Optional<TicketPriority> findByName(String name);

    Optional<TicketPriority> findByNameAndDeletedAtIsNull(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<TicketPriority> findByColor(String color);
}