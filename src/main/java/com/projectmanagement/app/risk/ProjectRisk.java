package com.projectmanagement.app.risk;

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
@Table(name = "project_risks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRisk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    Project project;
    @Column(nullable = false, length = 255)
    String title;
    @Column(columnDefinition = "TEXT")
    String description;
    @Column(nullable = false, length = 30)
    String probability;
    @Column(nullable = false, length = 30)
    String impact;
    @Column(nullable = false, length = 30)
    String status;
    @Column(length = 255)
    String owner;
    @Column(columnDefinition = "TEXT")
    String mitigation;
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    void c() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}
