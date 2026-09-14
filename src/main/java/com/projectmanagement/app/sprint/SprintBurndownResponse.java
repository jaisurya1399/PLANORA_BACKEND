package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
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
public class SprintBurndownResponse {
    private Long sprintId;
    private String sprintName;
    private BigDecimal totalEstimate;
    private List<SprintProgressPointResponse> points;
}
