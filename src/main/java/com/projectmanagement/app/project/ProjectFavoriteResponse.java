package com.projectmanagement.app.project;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectFavoriteResponse {

    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    private Long projectId;
    private String projectName;
    private String projectTicketPrefix;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}