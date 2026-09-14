package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketSubscriberRepository
                extends JpaRepository<TicketSubscriber, Long> {

        List<TicketSubscriber> findByTicketId(Long ticketId);

        List<TicketSubscriber> findByUserId(Long userId);

        Optional<TicketSubscriber> findByTicketIdAndUserId(
                        Long ticketId,
                        Long userId);

        boolean existsByTicketIdAndUserId(
                        Long ticketId,
                        Long userId);

        long countByTicketId(Long ticketId);

        long countByUserId(Long userId);

        void deleteByTicketId(Long ticketId);

        void deleteByUserId(Long userId);
}