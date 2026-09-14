package com.projectmanagement.app.enterprisereport;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EnterprisePortfolioReportResponse {
    Long portfolioId;
    String portfolioName;
    long projects;
    long totalIssues;
    long resolvedIssues;
    long overdueIssues;
    long highRisks;
    long slaBreaches;
    long blockedDependencies;
    Map<String, Integer> projectHealth;
}
