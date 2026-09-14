package com.projectmanagement.app.timetracking;

import java.time.LocalDateTime;

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
public class TimeTrackingTimerResponse {
    private Long id;
    private Long ticketId;
    private String ticketCode;
    private String ticketName;
    private Long userId;
    private LocalDateTime startedAt;
    private String description;
    private long elapsedSeconds;
}
