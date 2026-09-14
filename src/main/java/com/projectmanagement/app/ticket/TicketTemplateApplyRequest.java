package com.projectmanagement.app.ticket;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TicketTemplateApplyRequest {
    @NotNull
    @Positive
    Long ownerId;
    @Positive
    Long responsibleId;
    String name;
    String content;
    @Positive
    Long statusId;
    @Positive
    Long priorityId;
    @PositiveOrZero
    BigDecimal estimation;
    Map<String, String> customFields;
}
