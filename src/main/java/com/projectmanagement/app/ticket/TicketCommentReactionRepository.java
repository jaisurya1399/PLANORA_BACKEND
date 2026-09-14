package com.projectmanagement.app.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentReactionRepository extends JpaRepository<TicketCommentReaction, Long> {
    List<TicketCommentReaction> findByCommentIdOrderByCreatedAtAsc(Long commentId);

    Optional<TicketCommentReaction> findByCommentIdAndUserIdAndReaction(Long commentId, Long userId, String reaction);

    long countByCommentIdAndReaction(Long commentId, String reaction);

    void deleteByCommentId(Long commentId);
}
