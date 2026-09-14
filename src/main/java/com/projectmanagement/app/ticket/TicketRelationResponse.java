package com.projectmanagement.app.ticket;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRelationResponse {

    private Long id;

    private Long ticketId;
    private String ticketName;
    private String ticketCode;

    private Long relationId;
    private String relationName;
    private String relationCode;

    private String type;

    private Integer sort;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}