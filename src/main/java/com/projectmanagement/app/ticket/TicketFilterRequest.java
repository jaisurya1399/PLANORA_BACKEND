package com.projectmanagement.app.ticket;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class TicketFilterRequest {
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
    @Min(0)
    @Builder.Default
    private int page = 0;
    @Min(1)
    @Max(100)
    @Builder.Default
    private int size = 50;
    @Builder.Default
    private String sort = "order";
    @Builder.Default
    private String direction = "ASC";
}
