package com.projectmanagement.app.epic;

import java.time.LocalDate;
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
public class EpicResponse {

    private Long id;

    private Long projectId;
    private String projectName;

    private Long milestoneId;
    private String milestoneName;

    private String name;

    private LocalDate startsAt;
    private LocalDate endsAt;

    private Long parentId;
    private String parentName;

    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}