package com.projectmanagement.app.release;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
public class ReleaseBurndownResponse {
    private Long releaseId;
    private String version;
    private LocalDate startsAt;
    private LocalDate endsAt;
    private BigDecimal totalEstimation;
    private List<Point> points;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Point {
        private LocalDate date;
        private BigDecimal idealRemaining;
        private BigDecimal remaining;
        private BigDecimal completed;
    }
}
