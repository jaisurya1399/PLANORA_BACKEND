package com.projectmanagement.app.ticket;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TicketPlanningRequest {
    @NotEmpty(message = "At least one ticket ID is required")
    private List<@NotNull @Positive Long> ticketIds;
    @Positive
    private Long statusId;
    @Positive
    private Long sprintId;
    @Builder.Default
    private Boolean moveToBacklog = false;
}
