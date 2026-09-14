package com.projectmanagement.app.ticket;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TicketTemplateRequest {
    @NotNull
    @Positive
    Long projectId;
    @NotNull
    @Positive
    Long ticketTypeId;
    @NotNull
    @Positive
    Long statusId;
    @NotNull
    @Positive
    Long priorityId;
    @NotBlank
    String name;
    String content;
    @PositiveOrZero
    BigDecimal estimation;
    String customFieldsJson;
    Boolean active;
}
