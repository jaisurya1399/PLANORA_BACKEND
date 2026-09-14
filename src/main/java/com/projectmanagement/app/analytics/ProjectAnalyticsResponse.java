package com.projectmanagement.app.analytics;

import java.math.BigDecimal;
import java.util.List;

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
public class ProjectAnalyticsResponse {
    private Long projectId;
    private long totalIssues;
    private long openIssues;
    private long completedIssues;
    private long cancelledIssues;
    private long backlogIssues;
    private long inProgressIssues;
    private BigDecimal totalEstimate;
    private BigDecimal completedEstimate;
    private BigDecimal completionPercent;
    private BigDecimal averageLeadHours;
    private BigDecimal averageCycleHours;
    private List<AssigneeWorkload> workload;
    private List<SprintVelocity> velocity;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssigneeWorkload {
        private Long userId;
        private String userName;
        private long openIssues;
        private long completedIssues;
        private BigDecimal openEstimate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SprintVelocity {
        private Long sprintId;
        private String sprintName;
        private long completedIssues;
        private BigDecimal completedEstimate;
    }
}
