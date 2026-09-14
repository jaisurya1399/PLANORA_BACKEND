package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

        List<TicketComment> findByDeletedAtIsNull();

        List<TicketComment> findByTicketId(Long ticketId);

        List<TicketComment> findByTicketIdAndDeletedAtIsNull(Long ticketId);

        List<TicketComment> findByTicketIdAndDeletedAtIsNullOrderByCreatedAtAsc(
                        Long ticketId);

        List<TicketComment> findByUserId(Long userId);

        List<TicketComment> findByUserIdAndDeletedAtIsNull(Long userId);

        long countByTicketId(Long ticketId);

        long countByTicketIdAndDeletedAtIsNull(Long ticketId);

        long countByUserId(Long userId);

        void deleteByTicketId(Long ticketId);

        void deleteByUserId(Long userId);
}