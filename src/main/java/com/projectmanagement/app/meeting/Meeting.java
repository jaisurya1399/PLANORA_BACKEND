package com.projectmanagement.app.meeting;

import java.time.LocalDateTime;

import com.projectmanagement.app.epic.Epic;
import com.projectmanagement.app.project.Project;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_meetings", indexes = {
        @Index(name = "idx_project_meetings_project_time", columnList = "project_id,starts_at"),
        @Index(name = "idx_project_meetings_epic", columnList = "epic_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;
    @Column(nullable = false, length = 255)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String agenda;
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;
    @Column(name = "ends_at")
    private LocalDateTime endsAt;
    @Column(name = "meeting_type", nullable = false, length = 20)
    private String meetingType;
    @Column(name = "meeting_url", length = 1000)
    private String meetingUrl;
    @Column(length = 1000)
    private String location;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(name = "invite_all_team", nullable = false)
    private boolean inviteAllTeam;
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void create() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (status == null)
            status = "SCHEDULED";
        if (meetingType == null)
            meetingType = "ONLINE";
    }
}
