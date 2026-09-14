package com.projectmanagement.app.dailyscrum;

import java.time.LocalDateTime;

import com.projectmanagement.app.project.Project;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_scrum_meetings", uniqueConstraints = @UniqueConstraint(name = "uk_daily_scrum_meeting_project", columnNames = "project_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScrumMeeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @Column(name = "meeting_title", nullable = false, length = 255)
    private String meetingTitle;
    @Column(name = "meeting_url", nullable = false, length = 1000)
    private String meetingUrl;
    @Column(name = "meeting_time", nullable = false)
    private String meetingTime;
    @Column(name = "active", nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @PrePersist void create(){ LocalDateTime n=LocalDateTime.now(); createdAt=n; updatedAt=n; }
    @PreUpdate void update(){ updatedAt=LocalDateTime.now(); }
}
