package com.projectmanagement.app.sprint;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SprintRequest {

    @NotBlank(message = "Sprint name is required")
    @Size(max = 255, message = "Sprint name cannot exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Sprint goal cannot exceed 5000 characters")
    private String goal;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private LocalDate startDate;

    private LocalDate endDate;

    private SprintStatus status;

    @AssertTrue(message = "End date cannot be before start date")
    public boolean isValidDateRange() {

        if (startDate == null || endDate == null) {
            return true;
        }

        return !endDate.isBefore(startDate);
    }
}