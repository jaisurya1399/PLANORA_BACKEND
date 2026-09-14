package com.projectmanagement.app.timetracking;

import java.math.BigDecimal;

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
public class TimeTrackingSummaryResponse {
    private Long ticketId;
    private String ticketCode;
    private String ticketName;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private BigDecimal remainingEstimateHours;
    private BigDecimal varianceHours;
}
