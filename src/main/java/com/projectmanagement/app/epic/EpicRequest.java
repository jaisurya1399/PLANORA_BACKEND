package com.projectmanagement.app.epic;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class EpicRequest {

    @NotNull
    @Positive
    private Long projectId;

    /**
     * Epic must belong to a Milestone.
     */
    @NotNull(message = "Milestone is required")
    @Positive(message = "Milestone ID must be greater than 0")
    private Long milestoneId;

    @NotBlank(message = "Epic name is required")
    @Size(max = 255, message = "Epic name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Start date is required")
    private LocalDate startsAt;

    @NotNull(message = "End date is required")
    private LocalDate endsAt;

    /**
     * Optional parent Epic.
     */
    @Positive(message = "Parent Epic ID must be greater than 0")
    private Long parentId;

    @AssertTrue(message = "End date must be on or after start date")
    public boolean isValidDateRange() {

        if (startsAt == null || endsAt == null) {
            return true;
        }

        return !endsAt.isBefore(startsAt);
    }
}