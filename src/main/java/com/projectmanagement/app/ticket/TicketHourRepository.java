package com.projectmanagement.app.ticket;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketHourRepository
                extends JpaRepository<TicketHour, Long> {

        List<TicketHour> findByTicketId(Long ticketId);

        List<TicketHour> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

        List<TicketHour> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

        List<TicketHour> findByUserId(Long userId);

        List<TicketHour> findByUserIdOrderByCreatedAtDesc(Long userId);

        List<TicketHour> findByActivityId(Long activityId);

        List<TicketHour> findByTicketIdAndUserId(
                        Long ticketId,
                        Long userId);

        long countByTicketId(Long ticketId);

        long countByUserId(Long userId);

        long countByActivityId(Long activityId);

        void deleteByTicketId(Long ticketId);

        void deleteByUserId(Long userId);

        void deleteByActivityId(Long activityId);
}