package com.projectmanagement.app.release;

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
public class ReleaseVersionProgressResponse {
    private Long releaseId;
    private String version;
    private long totalTickets;
    private long completedTickets;
    private long inProgressTickets;
    private long remainingTickets;
    private BigDecimal totalEstimation;
    private BigDecimal completedEstimation;
    private BigDecimal remainingEstimation;
    private int progressPercent;
}
