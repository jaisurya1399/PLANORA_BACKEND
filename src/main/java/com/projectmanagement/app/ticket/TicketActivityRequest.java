package com.projectmanagement.app.ticket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TicketActivityRequest {

    @NotNull(message = "Ticket ID is required")
    @Positive(message = "Ticket ID must be positive")
    private Long ticketId;

    @Positive(message = "Old status ID must be positive")
    private Long oldStatusId;

    @Positive(message = "New status ID must be positive")
    private Long newStatusId;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
}