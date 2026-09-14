package com.projectmanagement.app.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "project_member_availability", uniqueConstraints = @UniqueConstraint(name = "uk_project_member_availability_project_user_date", columnNames = {
                "project_id", "user_id", "availability_date" }), indexes = {
                                @Index(name = "idx_project_member_availability_project_date", columnList = "project_id, availability_date"),
                                @Index(name = "idx_project_member_availability_user_date", columnList = "user_id, availability_date")
                })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAvailability {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "project_member_availability_project_id_foreign"))
        private Project project;

        /**
         * Null user means the entry applies to the entire project (for example a
         * project holiday).
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "project_member_availability_user_id_foreign"))
        private User user;

        @Column(name = "availability_date", nullable = false)
        private LocalDate availabilityDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "availability_type", nullable = false, length = 20)
        private MemberAvailabilityType availabilityType;

        @Column(name = "available_hours", precision = 5, scale = 2)
        private BigDecimal availableHours;

        @Column(name = "reason", length = 500)
        private String reason;

        @Column(name = "created_at")
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
                LocalDateTime now = LocalDateTime.now();
                if (createdAt == null)
                        createdAt = now;
                if (updatedAt == null)
                        updatedAt = now;
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = LocalDateTime.now();
        }
}
