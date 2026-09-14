package com.projectmanagement.app.sprint;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SprintCapacityRequest {
    @NotNull private Long userId;
    @NotNull private BigDecimal capacityPoints;
    private BigDecimal capacityHours;
}
