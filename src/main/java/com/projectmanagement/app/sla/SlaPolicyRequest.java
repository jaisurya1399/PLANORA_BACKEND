package com.projectmanagement.app.sla;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlaPolicyRequest {
    Long projectId;
    String name;
    int targetHours;
    String priorityFilter;
    Boolean enabled;
}
