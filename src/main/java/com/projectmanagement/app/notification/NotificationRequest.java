package com.projectmanagement.app.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Type is required")
    @Size(max = 255, message = "Type cannot exceed 255 characters")
    private String type;

    @NotBlank(message = "Notifiable type is required")
    @Size(max = 255, message = "Notifiable type cannot exceed 255 characters")
    private String notifiableType;

    @NotNull(message = "Notifiable ID is required")
    @Positive(message = "Notifiable ID must be positive")
    private Long notifiableId;

    @NotBlank(message = "Data is required")
    private String data;
}