package com.projectmanagement.app.sprint;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SprintCapacityResponse {

    private Long id;
    private Long sprintId;
    private Long userId;
    private String userName;
    private String userEmail;

    /** Manually planned story-point capacity. */
    private BigDecimal capacityPoints;

    /** Automatically calculated available working hours for the sprint. */
    private BigDecimal capacityHours;

    /** Capacity before availability/weekend/holiday reductions. */
    private BigDecimal baseCapacityHours;

    /** Hours removed because of weekends, holidays, half-days or unavailability. */
    private BigDecimal reducedCapacityHours;

    private int workingDays;
    private int holidayDays;
    private int halfDayDays;
    private int unavailableDays;
    private int weekendDays;

    private BigDecimal assignedEstimate;

    /**
     * Story points still available after assigned work. Negative means overload.
     */
    private BigDecimal remainingCapacityPoints;

    /** HEALTHY, HIGH, OVERALLOCATED or NOT_CONFIGURED. */
    private String capacityStatus;

    /** Null when no story-point capacity has been configured. */
    private BigDecimal utilizationPercent;
}
