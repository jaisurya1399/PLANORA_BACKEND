package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository
        extends JpaRepository<TicketType, Long> {

    List<TicketType> findByDeletedAtIsNull();

    List<TicketType> findByIsDefaultTrue();

    Optional<TicketType> findByName(String name);

    Optional<TicketType> findByNameAndDeletedAtIsNull(
            String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(
            String name,
            Long id);

    List<TicketType> findByIcon(String icon);

    List<TicketType> findByColor(String color);
}