package com.projectmanagement.app.ticket;

import java.time.LocalDateTime;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_relations", indexes = {
                @Index(name = "ticket_relations_ticket_id_foreign", columnList = "ticket_id"),
                @Index(name = "ticket_relations_relation_id_foreign", columnList = "relation_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRelation {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "ticket_relations_ticket_id_foreign"))
        private Ticket ticket;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "relation_id", nullable = false, foreignKey = @ForeignKey(name = "ticket_relations_relation_id_foreign"))
        private Ticket relation;

        @Column(name = "type", nullable = false, length = 255)
        private String type;

        @Column(name = "sort", nullable = false)
        @Builder.Default
        private Integer sort = 1;

        @Column(name = "created_at")
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
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

                if (sort == null) {
                        sort = 1;
                }
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = LocalDateTime.now();
        }
}