package com.projectmanagement.app.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
public class DeveloperDashboardResponse {

    private long myProjects;

    private long myTasks;

    private long pendingTasks;

    private BigDecimal hoursThisWeek;

    private List<ProjectSummary> projects;

    private List<RecentActivity> recentActivity;

    // ============================================================
    // PROJECT SUMMARY
    // ============================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectSummary {

        private Long id;

        private String name;

        private String description;

        private String ticketPrefix;

        private String statusType;

        private String projectStatus;

        private Long ownerId;

        private String ownerName;

        private String ownerEmail;

        private String assignedRole;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;
    }

    // ============================================================
    // RECENT ACTIVITY
    // ============================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentActivity {

        private Long id;

        private Long ticketId;

        private String ticketName;

        private String oldStatus;

        private String newStatus;

        private String userName;

        private LocalDateTime createdAt;
    }
}