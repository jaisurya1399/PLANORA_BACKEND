package com.projectmanagement.app.ticket;

import java.math.BigDecimal;
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
public class TicketHourResponse {

    private Long id;

    private Long ticketId;
    private String ticketName;
    private String ticketCode;

    private Long userId;
    private String userName;
    private String userEmail;

    private BigDecimal value;

    private String comment;

    private Long activityId;

    private Long activityOldStatusId;
    private Long activityNewStatusId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}