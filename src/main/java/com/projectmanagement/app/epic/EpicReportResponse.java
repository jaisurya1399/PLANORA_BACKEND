package com.projectmanagement.app.epic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
public class EpicReportResponse {

    private Long epicId;
    private String epicName;
    private Long projectId;
    private String projectName;

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

    private Map<String, Long> statusDistribution;
    private Map<String, Long> priorityDistribution;
    private List<IssueSummary> issues;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IssueSummary {
        private Long id;
        private String code;
        private String name;
        private String status;
        private String statusCategory;
        private String priority;
        private BigDecimal estimation;
        private Long responsibleId;
        private String responsibleName;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;
    }
}
