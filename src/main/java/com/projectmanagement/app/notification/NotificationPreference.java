package com.projectmanagement.app.notification;

import com.projectmanagement.app.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="notification_preferences", uniqueConstraints=@UniqueConstraint(name="uk_notification_preferences_user_event", columnNames={"user_id","event_type"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationPreference {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="event_type",nullable=false,length=100) private String eventType;
 @Column(name="in_app_enabled",nullable=false) private boolean inAppEnabled=true;
 @Column(name="email_enabled",nullable=false) private boolean emailEnabled=true;
}
