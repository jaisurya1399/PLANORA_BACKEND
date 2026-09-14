package com.projectmanagement.app.timesheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeSheetCellResponse {

    private Long id;

    private Long timeSheetId;

    private Long userId;

    private Long projectId;

    private String task;

    private BigDecimal value;

    private Boolean isTrip;

    private String comment;

    private LocalDate date;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}