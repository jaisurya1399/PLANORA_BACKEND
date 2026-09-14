package com.projectmanagement.app.reminder;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReminderResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime remindAt;
    private String status;
    private LocalDateTime triggeredAt;
    private LocalDateTime createdAt;
}
