package com.projectmanagement.app.timetracking;

import java.time.LocalDateTime;

import com.projectmanagement.app.ticket.Ticket;
import com.projectmanagement.app.user.User;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "time_tracking_timers", indexes = {
                @Index(name = "idx_time_tracking_timer_user", columnList = "user_id"),
                @Index(name = "idx_time_tracking_timer_ticket", columnList = "ticket_id")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_time_tracking_timer_user_active", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeTrackingTimer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_time_tracking_timer_ticket"))
        private Ticket ticket;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_time_tracking_timer_user"))
        private User user;

        @Column(name = "started_at", nullable = false)
        private LocalDateTime startedAt;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
}
