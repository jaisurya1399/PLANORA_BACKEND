package com.projectmanagement.app.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.projectmanagement.app.epic.Epic;
import com.projectmanagement.app.label.Label;
import com.projectmanagement.app.milestone.Milestone;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.release.ReleaseVersion;
import com.projectmanagement.app.securityscheme.IssueSecurityLevel;
import com.projectmanagement.app.sprint.Sprint;
import com.projectmanagement.app.user.User;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    // =========================================================
    // PRIMARY KEY
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // BASIC INFORMATION
    // =========================================================

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "code", nullable = false, unique = true, length = 255)
    private String code;

    // =========================================================
    // USERS
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "tickets_owner_id_foreign"))
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id", foreignKey = @ForeignKey(name = "tickets_responsible_id_foreign"))
    private User responsible;

    // =========================================================
    // PROJECT
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "tickets_project_id_foreign"))
    private Project project;

    // =========================================================
    // TYPE / STATUS / PRIORITY
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false, foreignKey = @ForeignKey(name = "tickets_type_id_foreign"))
    private TicketType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false, foreignKey = @ForeignKey(name = "tickets_status_id_foreign"))
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "priority_id", nullable = false, foreignKey = @ForeignKey(name = "tickets_priority_id_foreign"))
    private TicketPriority priority;

    // =========================================================
    // SECURITY
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "security_level", nullable = false, length = 30)
    @Builder.Default
    private IssueSecurityLevel securityLevel = IssueSecurityLevel.PROJECT;

    // =========================================================
    // ORDER / ESTIMATION
    // =========================================================

    @Column(name = "\"order\"", nullable = false)
    @Builder.Default
    private Integer order = 0;

    @Column(name = "estimation", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal estimation = BigDecimal.ZERO;

    // =========================================================
    // EPIC / PARENT
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id", foreignKey = @ForeignKey(name = "tickets_epic_id_foreign"))
    private Epic epic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "tickets_parent_id_foreign"))
    private Ticket parent;

    // =========================================================
    // SPRINT
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", foreignKey = @ForeignKey(name = "tickets_sprint_id_foreign"))
    private Sprint sprint;

    // =========================================================
    // RELEASE
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", foreignKey = @ForeignKey(name = "tickets_release_id_foreign"))
    private ReleaseVersion release;

    // =========================================================
    // MILESTONE
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", foreignKey = @ForeignKey(name = "tickets_milestone_id_foreign"))
    private Milestone milestone;

    // =========================================================
    // LABELS
    // =========================================================

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ticket_labels", joinColumns = @JoinColumn(name = "ticket_id", foreignKey = @ForeignKey(name = "ticket_labels_ticket_id_foreign")), inverseJoinColumns = @JoinColumn(name = "label_id", foreignKey = @ForeignKey(name = "ticket_labels_label_id_foreign")))
    @Builder.Default
    private Set<Label> labels = new HashSet<>();

    // =========================================================
    // DUE DATE / OVERDUE TRACKING
    // =========================================================

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /**
     * Set by the overdue scheduler after the project admins have been
     * notified for the current due date. This prevents notification spam.
     */
    @Column(name = "overdue_notified_at")
    private LocalDateTime overdueNotifiedAt;

    // =========================================================
    // TIMESTAMPS
    // =========================================================

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================
    // CREATE
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (order == null) {
            order = 0;
        }

        if (estimation == null) {
            estimation = BigDecimal.ZERO;
        }

        if (securityLevel == null) {
            securityLevel = IssueSecurityLevel.PROJECT;
        }

        if (labels == null) {
            labels = new HashSet<>();
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}