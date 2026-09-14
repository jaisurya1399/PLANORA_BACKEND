package com.projectmanagement.app.project;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;

    private String name;

    private String description;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    private Long statusId;
    private String statusName;
    private String statusColor;

    private String ticketPrefix;

    private String statusType;

    private LocalDateTime deletedAt;

    private LocalDateTime archivedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
