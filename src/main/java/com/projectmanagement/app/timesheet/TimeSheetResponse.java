package com.projectmanagement.app.timesheet;

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
public class TimeSheetResponse {

    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    private Long projectId;
    private String projectName;

    private String task;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}