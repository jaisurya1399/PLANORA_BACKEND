package com.projectmanagement.app.sprint;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SprintStatisticsResponse {

    private Long sprintId;

    private String sprintName;

    private Long totalTickets;

    private Long assignedTickets;

    private Long backlogTickets;

    private BigDecimal totalEstimation;

    private BigDecimal completedEstimation;

    private BigDecimal remainingEstimation;

    private BigDecimal completionPercent;
}