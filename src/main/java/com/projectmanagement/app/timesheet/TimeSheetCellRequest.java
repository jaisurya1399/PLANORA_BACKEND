package com.projectmanagement.app.timesheet;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TimeSheetCellRequest {

    @NotNull(message = "Time sheet ID is required")
    @Positive(message = "Time sheet ID must be positive")
    private Long timeSheetId;

    @NotNull(message = "Value is required")
    @Digits(integer = 6, fraction = 2, message = "Value must have maximum 6 integer digits and 2 decimal digits")
    private BigDecimal value;

    private Boolean isTrip = false;

    private String comment;

    private LocalDate date;
}