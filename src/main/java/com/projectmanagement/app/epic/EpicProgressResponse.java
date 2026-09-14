package com.projectmanagement.app.epic;

import java.math.BigDecimal;

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
public class EpicProgressResponse {

    private Long epicId;
    private String epicName;
    private Long projectId;

    private long totalIssues;
    private long completedIssues;
    private long inProgressIssues;
    private long todoIssues;
    private long backlogIssues;
    private long cancelledIssues;

    private BigDecimal totalEstimation;
    private BigDecimal completedEstimation;
    private BigDecimal remainingEstimation;
    private double completionPercentage;
    private double estimationCompletionPercentage;
}
