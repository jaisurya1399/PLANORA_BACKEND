package com.projectmanagement.app.notificationscheme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification_scheme_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSchemeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    NotificationScheme scheme;
    @Column(name = "event_type", nullable = false, length = 100)
    String eventType;
    @Column(name = "recipient_type", nullable = false, length = 50)
    String recipientType;
    @Column(name = "in_app_enabled", nullable = false)
    boolean inAppEnabled = true;
    @Column(name = "email_enabled", nullable = false)
    boolean emailEnabled = true;
}
