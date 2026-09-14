package com.projectmanagement.app.enterprisereport;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EnterpriseReportResponse {
    Long projectId;
    String projectName;
    long totalIssues;
    long openIssues;
    long resolvedIssues;
    long overdueIssues;
    long highRisks;
    long blockedDependencies;
    long slaBreaches;
    int healthScore;
    String health;
    Map<String, Long> statusDistribution;
    Map<String, Long> priorityDistribution;
    Map<String, Long> assigneeWorkload;
}
