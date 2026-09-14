package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SprintCommitmentResponse {
    private Long sprintId;
    private String sprintName;
    private BigDecimal committedEstimate;
    private BigDecimal completedEstimate;
    private BigDecimal remainingCommittedEstimate;
    private BigDecimal scopeChangeEstimate;
    private Long committedTickets;
    private Long completedTickets;
    private Long currentTickets;
    private BigDecimal completionPercent;
}
