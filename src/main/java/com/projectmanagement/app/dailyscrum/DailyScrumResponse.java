package com.projectmanagement.app.dailyscrum;

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
public class DailyScrumResponse {

    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    private Long projectId;
    private String projectName;

    private LocalDate scrumDate;

    private String yesterdayWork;
    private String todayWork;
    private String blockers;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}