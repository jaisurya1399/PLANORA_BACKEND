package com.projectmanagement.app.dailyscrum;

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
public class DailyScrumRequest {

    @NotNull(message = "User is required")
    @Positive(message = "User ID must be valid")
    private Long userId;

    @NotNull(message = "Project is required")
    @Positive(message = "Project ID must be valid")
    private Long projectId;

    @NotNull(message = "Scrum date is required")
    private LocalDate scrumDate;

    @NotBlank(message = "Yesterday work is required")
    @Size(max = 10000)
    private String yesterdayWork;

    @NotBlank(message = "Today work is required")
    @Size(max = 10000)
    private String todayWork;

    @Size(max = 10000)
    private String blockers;

    @AssertTrue(message = "Scrum date cannot be in the future")
    public boolean isDateValid() {

        if (scrumDate == null) {
            return true;
        }

        return !scrumDate.isAfter(LocalDate.now());
    }
}