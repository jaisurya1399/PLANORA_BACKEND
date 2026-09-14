package com.projectmanagement.app.ticket;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketTemplateResponse {
    Long id, projectId, ticketTypeId, statusId, priorityId;
    String projectName, ticketTypeName, statusName, priorityName, name, content, customFieldsJson;
    BigDecimal estimation;
    Boolean active;
}
