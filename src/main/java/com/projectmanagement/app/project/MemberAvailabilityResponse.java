package com.projectmanagement.app.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberAvailabilityResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDate availabilityDate;
    private MemberAvailabilityType availabilityType;
    private BigDecimal availableHours;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
