package com.projectmanagement.app.milestone;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MilestoneRepository
                extends JpaRepository<Milestone, Long> {

        List<Milestone> findByProjectIdOrderByDueDateAscNameAsc(
                        Long projectId);

        List<Milestone> findByProjectIdAndStatusOrderByDueDateAsc(
                        Long projectId,
                        MilestoneStatus status);

        Optional<Milestone> findByIdAndProjectId(
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
                            WHERE t.milestone.id = :milestoneId
                        """)
        long countTickets(
                        @Param("milestoneId") Long milestoneId);

        @Query("""
                            SELECT COUNT(t)
                            FROM Ticket t
                            WHERE t.milestone.id = :milestoneId
                              AND UPPER(t.status.name) = 'DONE'
                        """)
        long countCompletedTickets(
                        @Param("milestoneId") Long milestoneId);

        @Query("""
                            SELECT t
                            FROM Ticket t
                            WHERE t.milestone.id = :milestoneId
                            ORDER BY t.order ASC, t.id ASC
                        """)
        List<com.projectmanagement.app.ticket.Ticket> findTickets(
                        @Param("milestoneId") Long milestoneId);
}