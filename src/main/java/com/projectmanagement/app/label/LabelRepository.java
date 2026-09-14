package com.projectmanagement.app.label;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findByProjectIdOrderByNameAsc(Long projectId);

    Optional<Label> findByIdAndProjectId(
            Long id,
            Long projectId);

    boolean existsByProjectIdAndNameIgnoreCase(
            Long projectId,
            String name);

    boolean existsByProjectIdAndNameIgnoreCaseAndIdNot(
            Long projectId,
            String name,
            Long id);

    @Query("""
                SELECT COUNT(t)
                FROM Ticket t
                JOIN t.labels l
                WHERE l.id = :labelId
            """)
    long countTicketsByLabelId(
            @Param("labelId") Long labelId);

    @Query("""
                SELECT DISTINCT l
                FROM Label l
                JOIN l.tickets t
                WHERE t.id = :ticketId
                ORDER BY l.name ASC
            """)
    List<Label> findByTicketId(
            @Param("ticketId") Long ticketId);
}