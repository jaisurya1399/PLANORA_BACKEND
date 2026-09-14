package com.projectmanagement.app.securityscheme;

import java.time.LocalDateTime;

import com.projectmanagement.app.project.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "issue_security_schemes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueSecurityScheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", unique = true)
    Project project;
    @Column(nullable = false)
    String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "default_level", nullable = false)
    IssueSecurityLevel defaultLevel;
    @Column(nullable = false)
    Boolean active;
    @Column(name = "created_at")
    LocalDateTime createdAt;
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    void c() {
        LocalDateTime n = LocalDateTime.now();
        if (createdAt == null)
            createdAt = n;
        if (updatedAt == null)
            updatedAt = n;
        if (defaultLevel == null)
            defaultLevel = IssueSecurityLevel.PROJECT;
        if (active == null)
            active = true;
    }

    @PreUpdate
    void u() {
        updatedAt = LocalDateTime.now();
    }
}
