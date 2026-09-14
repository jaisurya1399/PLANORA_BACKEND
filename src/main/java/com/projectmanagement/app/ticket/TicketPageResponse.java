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
public class TicketPageResponse {
    private List<TicketResponse> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean first;
    private boolean last;
}
