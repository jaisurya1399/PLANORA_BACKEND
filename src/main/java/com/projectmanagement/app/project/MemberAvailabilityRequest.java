package com.projectmanagement.app.project;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class MemberAvailabilityRequest {

    private Long userId;

    @NotNull
    private LocalDate availabilityDate;

    @NotNull
    private MemberAvailabilityType availabilityType;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "24.0")
    private BigDecimal availableHours;

    @Size(max = 500)
    private String reason;
}
