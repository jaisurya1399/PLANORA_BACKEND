package com.projectmanagement.app.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class ProjectWorkingHoursResponse {
    private Long id;
    private Long projectId;
    private LocalDate effectiveFrom;
    private BigDecimal workingHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
