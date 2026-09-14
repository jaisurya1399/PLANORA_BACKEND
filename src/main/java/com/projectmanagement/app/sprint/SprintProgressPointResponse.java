package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class SprintProgressPointResponse {
    private LocalDate date;
    private BigDecimal scopeEstimate;
    private BigDecimal completedEstimate;
    private BigDecimal remainingEstimate;
}
