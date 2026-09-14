package com.projectmanagement.app.timesheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "time_sheet_cells", indexes = {
                @Index(name = "time_sheet_cells_time_sheet_id_index", columnList = "time_sheet_id"),
                @Index(name = "time_sheet_cells_date_index", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSheetCell {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "time_sheet_id", nullable = false)
        private TimeSheet timeSheet;

        @Column(nullable = false, precision = 8, scale = 2)
        private BigDecimal value;

        @Column(name = "is_trip", nullable = false)
        @Builder.Default
        private Boolean isTrip = false;

        @Column(columnDefinition = "TEXT")
        private String comment;

        @Column
        private LocalDate date;

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

                if (isTrip == null) {
                        isTrip = false;
                }
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = LocalDateTime.now();
        }
}