package com.projectmanagement.app.meeting;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRequest {
    @NotBlank
    @Size(max = 255)
    private String title;
    @Size(max = 10000)
    private String agenda;
    @NotNull
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    @NotBlank
    private String meetingType;
    @Size(max = 1000)
    private String meetingUrl;
    @Size(max = 1000)
    private String location;
    private Long epicId;
    private boolean inviteAllTeam;
    private List<Long> attendeeIds;
}
