package com.projectmanagement.app.dailyscrum;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScrumMeetingResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String meetingTitle;
    private String meetingUrl;
    private String meetingTime;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
