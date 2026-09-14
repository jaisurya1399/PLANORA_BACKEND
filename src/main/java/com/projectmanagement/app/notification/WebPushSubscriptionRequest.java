package com.projectmanagement.app.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebPushSubscriptionRequest {
    @NotBlank @Size(max = 4096) private String endpoint;
    @NotBlank @Size(max = 512) private String p256dh;
    @NotBlank @Size(max = 512) private String auth;
}
