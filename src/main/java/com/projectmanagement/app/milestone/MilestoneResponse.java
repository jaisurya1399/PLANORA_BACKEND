package com.projectmanagement.app.milestone;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {

    private Long id;

    private String name;

    private String description;

    private Long projectId;

    private String projectName;

    private LocalDate startDate;

    private LocalDate dueDate;

    private MilestoneStatus status;

    private Integer progressPercent;

    private Long totalTickets;

    private Long completedTickets;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}