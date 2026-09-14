package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SprintResponse {

    private Long id;

    private String name;

    private String goal;

    private Long projectId;

    private String projectName;

    private LocalDate startDate;

    private LocalDate endDate;

    private SprintStatus status;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long ticketCount;

    private BigDecimal totalEstimation;
}