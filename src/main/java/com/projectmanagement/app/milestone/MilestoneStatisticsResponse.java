package com.projectmanagement.app.milestone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneStatisticsResponse {

    private Long milestoneId;

    private String milestoneName;

    private Long totalTickets;

    private Long completedTickets;

    private Long openTickets;

    private Integer progressPercent;

    private boolean overdue;
}