package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRelationRepository
                extends JpaRepository<TicketRelation, Long> {

        List<TicketRelation> findByTicketId(Long ticketId);

        List<TicketRelation> findByTicketIdOrderBySortAsc(Long ticketId);

        List<TicketRelation> findByRelationId(Long relationId);

        List<TicketRelation> findByType(String type);

        List<TicketRelation> findByTicketIdAndType(
                        Long ticketId,
                        String type);

        Optional<TicketRelation> findByTicketIdAndRelationIdAndType(
                        Long ticketId,
                        Long relationId,
                        String type);

        boolean existsByTicketIdAndRelationIdAndType(
                        Long ticketId,
                        Long relationId,
                        String type);

        long countByTicketId(Long ticketId);

        long countByRelationId(Long relationId);

        void deleteByTicketId(Long ticketId);

        void deleteByRelationId(Long relationId);
}