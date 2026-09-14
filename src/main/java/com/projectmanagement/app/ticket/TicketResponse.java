package com.projectmanagement.app.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import com.projectmanagement.app.securityscheme.IssueSecurityLevel;

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
public class TicketResponse {

    private Long id;

    private String name;

    private String content;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    private Long responsibleId;
    private String responsibleName;
    private String responsibleEmail;

    private Long statusId;
    private String statusName;
    private String statusColor;
    private TicketStatusCategory statusCategory;

    private Long projectId;
    private String projectName;

    private String code;

    private Long typeId;
    private String typeName;
    private String typeIcon;
    private String typeColor;

    private Integer order;

    private Long priorityId;
    private String priorityName;
    private String priorityColor;

    private BigDecimal estimation;

    private Long epicId;
    private String epicName;

    private Long parentId;
    private String parentCode;
    private String parentName;
    private long childCount;

    private Long sprintId;
    private String sprintName;

    private Long milestoneId;
    private String milestoneName;

    private Long releaseId;
    private String releaseVersion;

    private Set<Long> labelIds;

    private Map<String, String> customFields;

    private IssueSecurityLevel securityLevel;

    private LocalDateTime deletedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime dueDate;
    private LocalDateTime overdueNotifiedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
