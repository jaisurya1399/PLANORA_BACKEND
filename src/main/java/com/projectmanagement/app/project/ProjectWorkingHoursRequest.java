package com.projectmanagement.app.project;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class ProjectWorkingHoursRequest {

    @NotNull
    private LocalDate effectiveFrom;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "24.0")
    private BigDecimal workingHours;
}
