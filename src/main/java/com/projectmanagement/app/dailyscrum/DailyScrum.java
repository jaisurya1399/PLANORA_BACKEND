package com.projectmanagement.app.dailyscrum;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_scrums", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_scrum_user_project_date", columnNames = {
                "user_id",
                "project_id",
                "scrum_date"
        })
}, indexes = {
        @Index(name = "idx_daily_scrums_user_id", columnList = "user_id"),
        @Index(name = "idx_daily_scrums_project_id", columnList = "project_id"),
        @Index(name = "idx_daily_scrums_scrum_date", columnList = "scrum_date"),
        @Index(name = "idx_daily_scrums_user_date", columnList = "user_id, scrum_date"),
        @Index(name = "idx_daily_scrums_project_date", columnList = "project_id, scrum_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyScrum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "scrum_date", nullable = false)
    private LocalDate scrumDate;

    @Column(name = "yesterday_work", nullable = false, columnDefinition = "TEXT")
    private String yesterdayWork;

    @Column(name = "today_work", nullable = false, columnDefinition = "TEXT")
    private String todayWork;

    @Column(name = "blockers", columnDefinition = "TEXT")
    private String blockers;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (blockers == null) {
            blockers = "";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}