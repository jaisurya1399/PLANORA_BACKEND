package com.projectmanagement.app.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProjectHealthResponse {
    Long projectId;
    String projectName;
    String health;
    int score;
    long totalTickets;
    long overdueTickets;
    long openHighRisks;
    long blockedDependencies;
    long breachedSla;
    String rationale;
}
