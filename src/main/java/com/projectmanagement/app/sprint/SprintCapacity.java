package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.projectmanagement.app.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sprint_capacities", uniqueConstraints = @UniqueConstraint(name = "uk_sprint_capacities_sprint_user", columnNames = {"sprint_id", "user_id"}), indexes = {
        @Index(name = "idx_sprint_capacities_sprint_id", columnList = "sprint_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SprintCapacity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id", nullable = false, foreignKey = @ForeignKey(name = "sprint_capacities_sprint_id_foreign"))
    private Sprint sprint;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "sprint_capacities_user_id_foreign"))
    private User user;
    @Column(name = "capacity_points", nullable = false, precision = 10, scale = 2)
    @Builder.Default private BigDecimal capacityPoints = BigDecimal.ZERO;
    @Column(name = "capacity_hours", nullable = false, precision = 10, scale = 2)
    @Builder.Default private BigDecimal capacityHours = BigDecimal.ZERO;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
}
