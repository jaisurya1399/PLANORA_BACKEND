package com.projectmanagement.app.ticket;

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
public class TicketSavedViewResponse {
    private Long id;
    private Long projectId;
    private String name;
    private String q;
    private Long statusId;
    private Long priorityId;
    private Long responsibleId;
    private Long sprintId;
    private Long epicId;
    private Long labelId;
    private Boolean rootOnly;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
