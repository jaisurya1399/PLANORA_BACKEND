package com.projectmanagement.app.team;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectTeamResponse {
    Long id, projectId;
    String projectName, name, description;
    Boolean active;
    List<Long> memberIds;
    List<String> memberNames;
}
