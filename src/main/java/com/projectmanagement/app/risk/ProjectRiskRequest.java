package com.projectmanagement.app.risk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRiskRequest {
    Long projectId;
    String title;
    String description;
    String probability;
    String impact;
    String status;
    String owner;
    String mitigation;
}
