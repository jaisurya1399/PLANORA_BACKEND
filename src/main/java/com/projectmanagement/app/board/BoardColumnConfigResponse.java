package com.projectmanagement.app.board;

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
public class BoardColumnConfigResponse {
    private Long id;
    private Long statusId;
    private String statusName;
    private String statusColor;
    private String category;
    private String displayName;
    private Integer displayOrder;
    private Boolean enabled;
    private Integer wipLimit;
    private Integer ticketCount;
}
