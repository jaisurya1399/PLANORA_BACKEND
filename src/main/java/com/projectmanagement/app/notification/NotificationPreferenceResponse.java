package com.projectmanagement.app.notification;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationPreferenceResponse { private Long id; private Long userId; private String eventType; private boolean inAppEnabled; private boolean emailEnabled; }
