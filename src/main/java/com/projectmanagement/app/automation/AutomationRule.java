package com.projectmanagement.app.automation;

import java.time.LocalDateTime;

import com.projectmanagement.app.project.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "automation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "trigger_event", nullable = false, length = 80)
    private String triggerEvent;
    @Column(nullable = false, length = 80)
    private String conditionExpression;
    @Column(name = "action_type", nullable = false, length = 80)
    private String actionType;
    @Column(name = "action_value", columnDefinition = "TEXT")
    private String actionValue;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void create() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}
