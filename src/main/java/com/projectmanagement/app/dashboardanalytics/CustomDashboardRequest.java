package com.projectmanagement.app.dashboardanalytics;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomDashboardRequest {
    private String name;
    private Long projectId;
    private List<String> widgets;
}
