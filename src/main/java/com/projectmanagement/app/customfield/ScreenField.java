package com.projectmanagement.app.customfield;

import java.time.LocalDateTime;

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
@Table(name = "screen_fields", uniqueConstraints = @UniqueConstraint(name = "uq_screen_field", columnNames = {
        "screen_id", "field_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private ScreenConfiguration screen;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    private CustomField field;
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
    @Column(nullable = false)
    @Builder.Default
    private Boolean visible = true;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void c() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (displayOrder == null)
            displayOrder = 0;
        if (visible == null)
            visible = true;
    }
}
