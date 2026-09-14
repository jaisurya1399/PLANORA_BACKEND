package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketSavedViewRepository extends JpaRepository<TicketSavedView, Long> {
    List<TicketSavedView> findByProjectIdAndOwnerIdOrderByNameAsc(Long projectId, Long ownerId);

    Optional<TicketSavedView> findByIdAndProjectIdAndOwnerId(Long id, Long projectId, Long ownerId);

    boolean existsByProjectIdAndOwnerIdAndNameIgnoreCase(Long projectId, Long ownerId, String name);

    boolean existsByProjectIdAndOwnerIdAndNameIgnoreCaseAndIdNot(Long projectId, Long ownerId, String name, Long id);
}
