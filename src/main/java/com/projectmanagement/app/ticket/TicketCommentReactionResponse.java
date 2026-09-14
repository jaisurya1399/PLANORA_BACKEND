package com.projectmanagement.app.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCommentReactionResponse {
    private Long id;
    private Long commentId;
    private Long userId;
    private String userName;
    private String reaction;
    private java.time.LocalDateTime createdAt;
}
