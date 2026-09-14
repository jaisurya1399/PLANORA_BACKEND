package com.projectmanagement.app.ticket;

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
public class TicketStatusResponse {

    private Long id;

    private String name;

    private String color;

    private Boolean isDefault;

    private Integer order;

    private TicketStatusCategory category;

    private Long projectId;

    private String projectName;

    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
