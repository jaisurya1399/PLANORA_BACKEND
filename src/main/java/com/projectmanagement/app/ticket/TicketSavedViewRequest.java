package com.projectmanagement.app.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
public class TicketSavedViewRequest {
    @NotBlank
    @Size(max = 120)
    private String name;
    @Size(max = 255)
    private String q;
    @Positive
    private Long statusId;
    @Positive
    private Long priorityId;
    @Positive
    private Long responsibleId;
    @Positive
    private Long sprintId;
    @Positive
    private Long epicId;
    @Positive
    private Long labelId;
    @Builder.Default
    private Boolean rootOnly = false;
}
