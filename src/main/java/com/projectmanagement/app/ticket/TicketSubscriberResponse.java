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
public class TicketSubscriberResponse {

    private Long id;

    private Long ticketId;
    private String ticketName;
    private String ticketCode;

    private Long userId;
    private String userName;
    private String userEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}