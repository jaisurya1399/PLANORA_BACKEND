package com.projectmanagement.app.notification;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationPreferenceRequest { @NotBlank private String eventType; private boolean inAppEnabled=true; private boolean emailEnabled=true; }
