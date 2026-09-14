package com.projectmanagement.app.timetracking;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeTrackingTimerRepository extends JpaRepository<TimeTrackingTimer, Long> {
    Optional<TimeTrackingTimer> findByUserId(Long userId);

    Optional<TimeTrackingTimer> findByUserIdAndTicketId(Long userId, Long ticketId);
}
