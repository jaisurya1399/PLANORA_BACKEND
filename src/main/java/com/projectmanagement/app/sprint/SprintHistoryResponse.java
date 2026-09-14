package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SprintHistoryResponse {
    private Long sprintId;
    private String sprintName;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long committedTickets;
    private Long completedTickets;
    private BigDecimal committedEstimate;
    private BigDecimal completedEstimate;
    private BigDecimal completionPercent;
}
