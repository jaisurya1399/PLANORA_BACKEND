package com.projectmanagement.app.release;

import java.math.BigDecimal;

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
public class ReleaseTicketResponse {
    private Long id;
    private String code;
    private String name;
    private String status;
    private String statusCategory;
    private BigDecimal estimation;
}
