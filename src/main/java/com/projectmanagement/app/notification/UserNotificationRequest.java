package com.projectmanagement.app.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationRequest {

    // ============================================================
    // Notification Type
    // ============================================================

    @NotBlank(message = "Notification type is required")
    @Size(max = 255, message = "Notification type must not exceed 255 characters")
    private String type;

    // ============================================================
    // Notification Data
    // ============================================================

    /*
     * JSON/string payload stored in notifications.data.
     *
     * Example:
     * {"ticketId":10,"message":"New ticket created"}
     */
    private String data;
}