package com.projectmanagement.app.ticket;

import java.time.LocalDateTime;

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
public class TicketActivityResponse {

    private Long id;

    private Long ticketId;
    private String ticketName;

    private Long oldStatusId;
    private String oldStatusName;

    private Long newStatusId;
    private String newStatusName;

    private Long userId;
    private String userName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}