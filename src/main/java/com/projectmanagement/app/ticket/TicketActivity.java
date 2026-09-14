package com.projectmanagement.app.ticket;

import java.time.LocalDateTime;

import com.projectmanagement.app.user.User;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_activities", indexes = {
                @Index(name = "ticket_activities_ticket_id_index", columnList = "ticket_id"),
                @Index(name = "ticket_activities_user_id_index", columnList = "user_id"),
                @Index(name = "ticket_activities_old_status_id_index", columnList = "old_status_id"),
                @Index(name = "ticket_activities_new_status_id_index", columnList = "new_status_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketActivity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "ticket_id", nullable = false)
        private Ticket ticket;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "old_status_id")
        private TicketStatus oldStatus;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "new_status_id")
        private TicketStatus newStatus;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
                LocalDateTime now = LocalDateTime.now();

                if (createdAt == null) {
                        createdAt = now;
                }

                if (updatedAt == null) {
                        updatedAt = now;
                }
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = LocalDateTime.now();
        }
}