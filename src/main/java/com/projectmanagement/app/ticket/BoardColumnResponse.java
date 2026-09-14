package com.projectmanagement.app.ticket;

import java.util.List;

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
public class BoardColumnResponse {
    private Long statusId;
    private String statusName;
    private String statusColor;
    private TicketStatusCategory category;
    private Integer order;
    private List<TicketResponse> tickets;
}
