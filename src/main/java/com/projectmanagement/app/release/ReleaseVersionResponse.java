package com.projectmanagement.app.release;

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
public class ReleaseVersionResponse {
    private Long id;
    private String version;
    private String name;
    private String description;
    private String releaseNotes;
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate releaseDate;
    private ReleaseStatus status;
    private long totalTickets;
    private long completedTickets;
    private long remainingTickets;
    private int progressPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
