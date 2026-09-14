package com.projectmanagement.app.project;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private String ticketPrefix;
    private Long statusId;
    private String statusName;
    private String statusColor;
    private String statusType;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
