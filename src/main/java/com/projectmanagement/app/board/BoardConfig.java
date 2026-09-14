package com.projectmanagement.app.board;

import java.time.LocalDateTime;

import com.projectmanagement.app.project.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "board_configs", uniqueConstraints = @UniqueConstraint(name = "uk_board_configs_project", columnNames = "project_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "board_configs_project_id_foreign"))
    private Project project;
    @Enumerated(EnumType.STRING)
    @Column(name = "swimlane_type", nullable = false, length = 20)
    @Builder.Default
    private BoardSwimlaneType swimlaneType = BoardSwimlaneType.NONE;
    @Column(name = "enforce_wip", nullable = false)
    @Builder.Default
    private Boolean enforceWip = false;
    @Column(name = "active_sprint_only", nullable = false)
    @Builder.Default
    private Boolean activeSprintOnly = true;
    @Column(name = "show_epic", nullable = false)
    @Builder.Default
    private Boolean showEpic = true;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}
