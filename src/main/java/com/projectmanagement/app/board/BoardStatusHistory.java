package com.projectmanagement.app.board;

import java.time.LocalDateTime;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.ticket.TicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
@Table(name = "board_status_history", indexes = {
        @Index(name = "idx_board_status_history_project_time", columnList = "project_id,changed_at"),
        @Index(name = "idx_board_status_history_ticket_time", columnList = "ticket_id,changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "board_status_history_project_id_foreign"))
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "board_status_history_ticket_id_foreign"))
    private Ticket ticket;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_status_id", foreignKey = @ForeignKey(name = "board_status_history_from_status_foreign"))
    private TicketStatus fromStatus;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_status_id", nullable = false, foreignKey = @ForeignKey(name = "board_status_history_to_status_foreign"))
    private TicketStatus toStatus;
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (changedAt == null)
            changedAt = LocalDateTime.now();
    }
}
