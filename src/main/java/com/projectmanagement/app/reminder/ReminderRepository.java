package com.projectmanagement.app.reminder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);

    List<Reminder> findByUserIdAndStatusOrderByRemindAtAsc(Long userId, String status);

    Optional<Reminder> findByIdAndUserId(Long id, Long userId);

    List<Reminder> findTop200ByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(String status, LocalDateTime now);
}
