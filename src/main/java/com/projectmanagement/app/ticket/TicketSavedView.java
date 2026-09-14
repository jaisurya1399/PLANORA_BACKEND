package com.projectmanagement.app.ticket;

import java.time.LocalDateTime;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "ticket_saved_views", uniqueConstraints = @UniqueConstraint(name = "uk_ticket_saved_views_project_owner_name", columnNames = {
        "project_id", "owner_id", "name" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSavedView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "query_text", length = 255)
    private String queryText;
    @Column(name = "status_id")
    private Long statusId;
    @Column(name = "priority_id")
    private Long priorityId;
    @Column(name = "responsible_id")
    private Long responsibleId;
    @Column(name = "sprint_id")
    private Long sprintId;
    @Column(name = "epic_id")
    private Long epicId;
    @Column(name = "label_id")
    private Long labelId;
    @Column(name = "root_only", nullable = false)
    @Builder.Default
    private Boolean rootOnly = false;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void create() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (updatedAt == null)
            updatedAt = createdAt;
    }

    @PreUpdate
    void update() {
        updatedAt = LocalDateTime.now();
    }
}
