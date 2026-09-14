package com.projectmanagement.app.timesheet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TimeSheetRequest {

    @NotNull
    @Positive
    private Long userId;

    @Positive
    private Long projectId;

    @NotBlank
    private String task;
}