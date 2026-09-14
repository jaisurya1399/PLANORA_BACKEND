package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

        // ============================================================
        // ACTIVE / DELETED
        // ============================================================

        List<Ticket> findByDeletedAtIsNull();

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.deletedAt IS NULL AND t.resolvedAt IS NULL
                        ORDER BY t.id DESC
                        """)
        List<Ticket> findActiveTickets();

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.deletedAt IS NULL AND t.resolvedAt IS NOT NULL
                        ORDER BY t.id DESC
                        """)
        List<Ticket> findResolvedTickets();

        List<Ticket> findByDeletedAtIsNotNull();

        // ============================================================
        // PROJECT
        // ============================================================

        List<Ticket> findByProjectId(Long projectId);

        List<Ticket> findByProjectIdAndDeletedAtIsNull(
                        Long projectId);

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.project.id = :projectId
                          AND t.deletedAt IS NULL
                          AND t.resolvedAt IS NULL
                        ORDER BY t.order ASC, t.id ASC
                        """)
        List<Ticket> findActiveByProjectId(@Param("projectId") Long projectId);

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.project.id = :projectId
                          AND t.deletedAt IS NULL
                          AND t.resolvedAt IS NOT NULL
                        ORDER BY t.order ASC, t.id ASC
                        """)
        List<Ticket> findResolvedByProjectId(@Param("projectId") Long projectId);

        long countByProjectId(Long projectId);

        long countByProjectIdAndDeletedAtIsNull(
                        Long projectId);

        // ============================================================
        // OWNER
        // ============================================================

        List<Ticket> findByOwnerId(Long ownerId);

        List<Ticket> findByOwnerIdAndDeletedAtIsNull(
                        Long ownerId);

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.owner.id = :ownerId
                          AND t.deletedAt IS NULL
                          AND t.resolvedAt IS NULL
                        ORDER BY t.id DESC
                        """)
        List<Ticket> findActiveByOwnerId(@Param("ownerId") Long ownerId);

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.owner.id = :ownerId
                          AND t.deletedAt IS NULL
                          AND t.resolvedAt IS NOT NULL
                        ORDER BY t.id DESC
                        """)
        List<Ticket> findResolvedByOwnerId(@Param("ownerId") Long ownerId);

        long countByOwnerId(Long ownerId);

        // ============================================================
        // RESPONSIBLE
        // ============================================================

        List<Ticket> findByResponsibleId(Long responsibleId);

        List<Ticket> findByResponsibleIdAndDeletedAtIsNull(
                        Long responsibleId);

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.responsible.id = :responsibleId
                          AND t.deletedAt IS NULL
                          AND t.resolvedAt IS NULL
                        ORDER BY t.id DESC
                        """)
        List<Ticket> findActiveByResponsibleId(@Param("responsibleId") Long responsibleId);

        @Query("""
                        SELECT t FROM Ticket t
                        WHERE t.responsible.id = :responsibleId
                          AND t.deletedAt IS NULL
                          AND t.resolvedAt IS NOT NULL
                        ORDER BY t.id DESC
                        """)
        List<Ticket> findResolvedByResponsibleId(@Param("responsibleId") Long responsibleId);

        long countByResponsibleId(Long responsibleId);

        // ============================================================
        // STATUS
        // ============================================================

        List<Ticket> findByStatusId(Long statusId);

        List<Ticket> findByStatusIdAndDeletedAtIsNull(
                        Long statusId);

        long countByStatusId(Long statusId);

        // ============================================================
        // TYPE
        // ============================================================

        List<Ticket> findByTypeId(Long typeId);

        List<Ticket> findByTypeIdAndDeletedAtIsNull(
                        Long typeId);

        // ============================================================
        // PRIORITY
        // ============================================================

        List<Ticket> findByPriorityId(Long priorityId);

        List<Ticket> findByPriorityIdAndDeletedAtIsNull(
                        Long priorityId);

        // ============================================================
        // EPIC
        // ============================================================

        List<Ticket> findByEpicId(Long epicId);

        List<Ticket> findByEpicIdAndDeletedAtIsNull(
                        Long epicId);

        // ============================================================
        // PROJECT + STATUS
        // ============================================================

        List<Ticket> findByProjectIdAndStatusId(
                        Long projectId,
                        Long statusId);

        List<Ticket> findByProjectIdAndStatusIdAndDeletedAtIsNull(
                        Long projectId,
                        Long statusId);

        // ============================================================
        // PROJECT + TYPE
        // ============================================================

        List<Ticket> findByProjectIdAndTypeId(
                        Long projectId,
                        Long typeId);

        // ============================================================
        // PROJECT + PRIORITY
        // ============================================================

        List<Ticket> findByProjectIdAndPriorityId(
                        Long projectId,
                        Long priorityId);

        // ============================================================
        // PROJECT + EPIC
        // ============================================================

        List<Ticket> findByProjectIdAndEpicId(
                        Long projectId,
                        Long epicId);

        List<Ticket> findByProjectIdAndEpicIdAndDeletedAtIsNull(
                        Long projectId,
                        Long epicId);

        // ============================================================
        // CODE
        // ============================================================

        Optional<Ticket> findByCode(String code);

        Optional<Ticket> findByCodeAndDeletedAtIsNull(
                        String code);

        boolean existsByCode(String code);

        boolean existsByCodeAndIdNot(
                        String code,
                        Long id);

        // ============================================================
        // NAME
        // ============================================================

        List<Ticket> findByNameContainingIgnoreCase(
                        String name);

        List<Ticket> findByProjectIdAndNameContainingIgnoreCase(
                        Long projectId,
                        String name);

        // ============================================================
        // ORDER
        // ============================================================

        List<Ticket> findByProjectIdOrderByOrderAsc(
                        Long projectId);

        List<Ticket> findByProjectIdAndDeletedAtIsNullOrderByOrderAsc(
                        Long projectId);

        // ============================================================
        // PROJECT + STATUS + ORDER
        // ============================================================

        List<Ticket> findByProjectIdAndStatusIdAndDeletedAtIsNullOrderByOrderAsc(
                        Long projectId,
                        Long statusId);

        // ============================================================
        // DELETE BY PROJECT
        // ============================================================

        void deleteByProjectId(Long projectId);

        // ============================================================
        // DELETE BY EPIC
        // ============================================================

        void deleteByEpicId(Long epicId);

        // ============================================================
        // SPRINT
        // ============================================================

        List<Ticket> findBySprintIdOrderByOrderAsc(
                        Long sprintId);

        List<Ticket> findByProjectIdAndSprintIsNullOrderByOrderAsc(
                        Long projectId);

        long countBySprintId(Long sprintId);

        long countByProjectIdAndSprintIsNull(
                        Long projectId);

        // ============================================================
        // PARENT / CHILD
        // ============================================================

        List<Ticket> findByParentIdAndDeletedAtIsNullOrderByOrderAscIdAsc(
                        Long parentId);

        List<Ticket> findByProjectIdAndParentIsNullAndDeletedAtIsNullOrderByOrderAscIdAsc(
                        Long projectId);

        long countByParentIdAndDeletedAtIsNull(
                        Long parentId);

        // ============================================================
        // SERVER-SIDE PROJECT FILTER
        // ============================================================

        @Query(value = """
                        SELECT DISTINCT t
                        FROM Ticket t
                        LEFT JOIN t.labels l
                        WHERE t.project.id = :projectId
                          AND t.deletedAt IS NULL
                          AND (
                                :q IS NULL
                                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR LOWER(t.content) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR LOWER(t.code) LIKE LOWER(CONCAT('%', :q, '%'))
                              )
                          AND (:statusId IS NULL OR t.status.id = :statusId)
                          AND (:priorityId IS NULL OR t.priority.id = :priorityId)
                          AND (:responsibleId IS NULL OR t.responsible.id = :responsibleId)
                          AND (:sprintId IS NULL OR t.sprint.id = :sprintId)
                          AND (:epicId IS NULL OR t.epic.id = :epicId)
                          AND (:labelId IS NULL OR l.id = :labelId)
                          AND (:rootOnly = false OR t.parent IS NULL)
                        """, countQuery = """
                        SELECT COUNT(DISTINCT t)
                        FROM Ticket t
                        LEFT JOIN t.labels l
                        WHERE t.project.id = :projectId
                          AND t.deletedAt IS NULL
                          AND (
                                :q IS NULL
                                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR LOWER(t.content) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR LOWER(t.code) LIKE LOWER(CONCAT('%', :q, '%'))
                              )
                          AND (:statusId IS NULL OR t.status.id = :statusId)
                          AND (:priorityId IS NULL OR t.priority.id = :priorityId)
                          AND (:responsibleId IS NULL OR t.responsible.id = :responsibleId)
                          AND (:sprintId IS NULL OR t.sprint.id = :sprintId)
                          AND (:epicId IS NULL OR t.epic.id = :epicId)
                          AND (:labelId IS NULL OR l.id = :labelId)
                          AND (:rootOnly = false OR t.parent IS NULL)
                        """)
        Page<Ticket> searchActiveByProject(
                        @Param("projectId") Long projectId,
                        @Param("q") String q,
                        @Param("statusId") Long statusId,
                        @Param("priorityId") Long priorityId,
                        @Param("responsibleId") Long responsibleId,
                        @Param("sprintId") Long sprintId,
                        @Param("epicId") Long epicId,
                        @Param("labelId") Long labelId,
                        @Param("rootOnly") boolean rootOnly,
                        Pageable pageable);

        @Query("""
                        SELECT t
                        FROM Ticket t
                        WHERE t.deletedAt IS NULL
                          AND t.dueDate IS NOT NULL
                          AND t.dueDate < :now
                          AND t.resolvedAt IS NULL
                          AND t.overdueNotifiedAt IS NULL
                        ORDER BY t.dueDate ASC
                        """)
        List<Ticket> findOverdueNotYetNotified(@Param("now") java.time.LocalDateTime now);

        // ============================================================
        // LABEL
        // ============================================================

        @Query("""
                        SELECT t
                        FROM Ticket t
                        JOIN t.labels l
                        WHERE l.id = :labelId
                        ORDER BY t.id DESC
                        """)
        List<Ticket> findTicketsByLabelId(
                        @Param("labelId") Long labelId);

        @Query("""
                        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
                        FROM Ticket t
                        JOIN t.labels l
                        WHERE t.id = :ticketId
                          AND l.id = :labelId
                        """)
        boolean existsLabelOnTicket(
                        @Param("ticketId") Long ticketId,
                        @Param("labelId") Long labelId);

        // ============================================================
        // MILESTONE
        // ============================================================

        List<Ticket> findByMilestoneIdOrderByOrderAscIdAsc(
                        Long milestoneId);

        long countByMilestoneId(
                        Long milestoneId);

        @Query("""
                        SELECT COUNT(t)
                        FROM Ticket t
                        WHERE t.milestone.id = :milestoneId
                          AND LOWER(TRIM(t.status.name)) IN ('done', 'completed')
                        """)
        long countCompletedByMilestoneId(
                        @Param("milestoneId") Long milestoneId);
}