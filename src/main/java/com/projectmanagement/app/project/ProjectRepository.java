package com.projectmanagement.app.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

        // =========================================================
        // EXISTING QUERIES
        // =========================================================

        List<Project> findByDeletedAtIsNull();

        List<Project> findByArchivedAtIsNotNullAndDeletedAtIsNull();

        List<Project> findByArchivedAtIsNullAndDeletedAtIsNull();

        List<Project> findByOwnerId(Long ownerId);

        List<Project> findByOwnerIdAndDeletedAtIsNull(Long ownerId);

        List<Project> findByOwnerIdAndArchivedAtIsNullAndDeletedAtIsNull(Long ownerId);

        List<Project> findByStatusId(Long statusId);

        List<Project> findByStatusIdAndDeletedAtIsNull(Long statusId);

        List<Project> findByStatusIdAndArchivedAtIsNullAndDeletedAtIsNull(Long statusId);

        Optional<Project> findByName(String name);

        Optional<Project> findByNameAndDeletedAtIsNull(String name);

        boolean existsByName(String name);

        boolean existsByNameAndIdNot(String name, Long id);

        boolean existsByTicketPrefix(String ticketPrefix);

        boolean existsByTicketPrefixAndIdNot(
                        String ticketPrefix,
                        Long id);

        // =========================================================
        // PROJECT VISIBILITY
        // =========================================================

        /**
         * Returns projects visible to a particular user.
         *
         * A user can see a project when:
         *
         * 1. They are the owner
         * OR
         * 2. They are assigned to the project in project_users
         */
        @Query("""
                        SELECT DISTINCT p
                        FROM Project p
                        LEFT JOIN ProjectUser pu
                            ON pu.project.id = p.id
                        WHERE p.deletedAt IS NULL
                          AND (
                                p.owner.id = :userId
                                OR pu.user.id = :userId
                              )
                        ORDER BY p.id DESC
                        """)
        List<Project> findVisibleProjectsForUser(
                        @Param("userId") Long userId);

        /**
         * Returns active projects visible to a user.
         */
        @Query("""
                        SELECT DISTINCT p
                        FROM Project p
                        LEFT JOIN ProjectUser pu
                            ON pu.project.id = p.id
                        WHERE p.deletedAt IS NULL
                          AND p.archivedAt IS NULL
                          AND (
                                p.owner.id = :userId
                                OR pu.user.id = :userId
                              )
                        ORDER BY p.id DESC
                        """)
        List<Project> findVisibleActiveProjectsForUser(
                        @Param("userId") Long userId);

        /**
         * Check whether a user can access a particular project.
         */
        @Query("""
                        SELECT COUNT(p) > 0
                        FROM Project p
                        LEFT JOIN ProjectUser pu
                            ON pu.project.id = p.id
                        WHERE p.id = :projectId
                          AND (
                                p.owner.id = :userId
                                OR pu.user.id = :userId
                              )
                        """)
        boolean existsByIdAndUserCanAccess(
                        @Param("projectId") Long projectId,
                        @Param("userId") Long userId);
}