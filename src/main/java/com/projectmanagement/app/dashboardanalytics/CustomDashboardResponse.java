package com.projectmanagement.app.dashboardanalytics;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomDashboardResponse {
    private Long id;
    private String name;
    private Long projectId;
    private List<String> widgets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
