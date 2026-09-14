package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SprintReportResponse {
    private Long sprintId;
    private String sprintName;
    private SprintStatus status;
    private String goal;
    private Long committedTickets;
    private Long completedTickets;
    private Long incompleteTickets;
    private BigDecimal committedEstimate;
    private BigDecimal completedEstimate;
    private BigDecimal remainingEstimate;
    private BigDecimal completionPercent;
    private BigDecimal scopeChangeEstimate;
    private BigDecimal commitmentCompletionPercent;
    private List<SprintReportIssue> issues;

    @Getter
    @Builder
    public static class SprintReportIssue {
        private Long ticketId;
        private String code;
        private String name;
        private BigDecimal estimation;
        private String status;
        private String statusCategory;
        private boolean completed;
    }
}
