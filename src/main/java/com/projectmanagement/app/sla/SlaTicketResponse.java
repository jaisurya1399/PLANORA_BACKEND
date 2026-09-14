package com.projectmanagement.app.sla;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SlaTicketResponse {
    Long ticketId;
    String ticketCode;
    String priority;
    LocalDateTime startedAt;
    LocalDateTime dueAt;
    boolean breached;
    boolean resolved;
    long remainingMinutes;
}
