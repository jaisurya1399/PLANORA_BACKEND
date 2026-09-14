package com.projectmanagement.app.sprint;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintIssueSnapshotRepository extends JpaRepository<SprintIssueSnapshot, Long> {
    List<SprintIssueSnapshot> findBySprintIdOrderByTicketIdAsc(Long sprintId);

    boolean existsBySprintIdAndTicketId(Long sprintId, Long ticketId);

    Optional<SprintIssueSnapshot> findBySprintIdAndTicketId(Long sprintId, Long ticketId);
}
