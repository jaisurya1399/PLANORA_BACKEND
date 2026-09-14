package com.projectmanagement.app.dailyscrum;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScrumMeetingRequest {
    @NotNull @Positive private Long projectId;
    @NotBlank @Size(max=255) private String meetingTitle;
    @NotBlank @Size(max=1000) private String meetingUrl;
    @NotBlank @Size(max=20) private String meetingTime;
    private boolean active = true;
}
