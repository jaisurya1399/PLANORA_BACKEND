package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SprintVelocityResponse {
    private Long projectId;
    private BigDecimal averageVelocity;
    private BigDecimal totalCompletedEstimate;
    private List<SprintVelocityPoint> sprints;

    @Getter
    @Builder
    public static class SprintVelocityPoint {
        private Long sprintId;
        private String sprintName;
        private BigDecimal committedEstimate;
        private BigDecimal completedEstimate;
        private Long committedTickets;
        private Long completedTickets;
    }
}
