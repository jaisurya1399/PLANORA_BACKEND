package com.projectmanagement.app.dependency;

import java.time.LocalDateTime;

import com.projectmanagement.app.ticket.Ticket;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_dependencies", uniqueConstraints = @UniqueConstraint(columnNames = { "source_ticket_id",
        "target_ticket_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dependency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_ticket_id", nullable = false)
    Ticket sourceTicket;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_ticket_id", nullable = false)
    Ticket targetTicket;
    @Column(nullable = false, length = 30)
    String type;
    @Column(columnDefinition = "TEXT")
    String description;
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    void c() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}
