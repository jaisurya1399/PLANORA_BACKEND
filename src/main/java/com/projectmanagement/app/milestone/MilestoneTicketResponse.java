package com.projectmanagement.app.milestone;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneTicketResponse {

    private Long id;

    private String code;

    private String name;

    private Long projectId;

    private String projectName;

    private Long ownerId;

    private String ownerName;

    private Long responsibleId;

    private String responsibleName;

    private Long statusId;

    private String statusName;

    private Long priorityId;

    private String priorityName;

    private BigDecimal estimation;
}