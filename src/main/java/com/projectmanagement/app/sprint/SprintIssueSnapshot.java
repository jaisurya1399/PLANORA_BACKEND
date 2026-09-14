package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.projectmanagement.app.ticket.TicketStatusCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sprint_issue_snapshots", uniqueConstraints = @UniqueConstraint(name = "uk_sprint_issue_snapshots_sprint_ticket", columnNames = {
        "sprint_id", "ticket_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintIssueSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;
    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal estimation;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "final_status_category", nullable = false, length = 20)
    private TicketStatusCategory finalStatusCategory;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void create() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (estimation == null)
            estimation = BigDecimal.ZERO;
    }
}
