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
public class TicketSubscriberRequest {

    @NotNull
    @Positive
    private Long ticketId;

    @NotNull
    @Positive
    private Long userId;
}