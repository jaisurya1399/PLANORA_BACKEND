package com.projectmanagement.app.board;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardStatusHistoryRepository extends JpaRepository<BoardStatusHistory, Long> {
    List<BoardStatusHistory> findByProjectIdAndChangedAtBetweenOrderByChangedAtAsc(Long projectId, LocalDateTime from,
            LocalDateTime to);

    List<BoardStatusHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}
