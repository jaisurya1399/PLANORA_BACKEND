package com.projectmanagement.app.ticketattachment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketAttachmentRepository
                extends JpaRepository<TicketAttachment, Long> {

        List<TicketAttachment> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

        long countByTicketId(Long ticketId);

        List<TicketAttachment> findByComment_IdOrderByCreatedAtDesc(Long commentId);

        void deleteByTicketId(Long ticketId);
}
