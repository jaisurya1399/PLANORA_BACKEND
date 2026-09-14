package com.projectmanagement.app.board;

import java.time.LocalDate;
import java.util.Map;

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
public class CumulativeFlowPointResponse {
    private LocalDate date;
    private Map<Long, Integer> counts;
}
