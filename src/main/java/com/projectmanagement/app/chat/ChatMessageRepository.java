package com.projectmanagement.app.chat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop100ByRoomTypeAndRoomIdOrderByCreatedAtAsc(String roomType, Long roomId);

    List<ChatMessage> findTop100ByRoomTypeAndSenderIdAndRecipientIdOrRoomTypeAndSenderIdAndRecipientIdOrderByCreatedAtAsc(
            String a, Long b, Long c, String d, Long e, Long f);
}
