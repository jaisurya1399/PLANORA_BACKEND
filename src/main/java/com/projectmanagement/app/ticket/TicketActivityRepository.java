package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketActivityRepository
                extends JpaRepository<TicketActivity, Long> {

        List<TicketActivity> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

        List<TicketActivity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

        List<TicketActivity> findByUserIdOrderByCreatedAtDesc(Long userId);

        List<TicketActivity> findByOldStatusId(Long oldStatusId);

        List<TicketActivity> findByNewStatusId(Long newStatusId);

        long countByTicketId(Long ticketId);

        long countByUserId(Long userId);

        void deleteByTicketId(Long ticketId);

        List<TicketActivity> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}