package com.projectmanagement.app.ticket;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
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
public class TicketHourRequest {

    @NotNull
    @Positive
    private Long ticketId;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal value;

    private String comment;

    @Positive
    private Long activityId;
}