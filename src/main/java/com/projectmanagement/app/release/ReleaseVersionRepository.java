package com.projectmanagement.app.release;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projectmanagement.app.ticket.Ticket;

public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Long> {
    List<ReleaseVersion> findByProjectIdOrderByReleaseDateAscVersionAsc(Long projectId);

    Optional<ReleaseVersion> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByProjectIdAndVersionIgnoreCase(Long projectId, String version);

    boolean existsByProjectIdAndVersionIgnoreCaseAndIdNot(Long projectId, String version, Long id);

    @Query("select count(t) from Ticket t where t.release.id = :releaseId and t.deletedAt is null")
    long countTickets(@Param("releaseId") Long releaseId);

    @Query("select count(t) from Ticket t where t.release.id = :releaseId and t.deletedAt is null and t.status.category = com.projectmanagement.app.ticket.TicketStatusCategory.DONE")
    long countCompletedTickets(@Param("releaseId") Long releaseId);

    @Query("select t from Ticket t where t.release.id = :releaseId and t.deletedAt is null order by t.order asc, t.id asc")
    List<Ticket> findTickets(@Param("releaseId") Long releaseId);
}
