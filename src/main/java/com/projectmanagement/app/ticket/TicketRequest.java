package com.projectmanagement.app.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import com.projectmanagement.app.securityscheme.IssueSecurityLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class TicketRequest {

    @NotBlank(message = "Ticket name is required")
    @Size(max = 255, message = "Ticket name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Ticket content is required")
    private String content;

    @NotNull(message = "Owner ID is required")
    @Positive(message = "Owner ID must be positive")
    private Long ownerId;

    @Positive(message = "Responsible ID must be positive")
    private Long responsibleId;

    private LocalDateTime dueDate;

    @NotNull(message = "Status ID is required")
    @Positive(message = "Status ID must be positive")
    private Long statusId;

    @NotNull(message = "Project ID is required")
    @Positive(message = "Project ID must be positive")
    private Long projectId;

    @NotNull(message = "Ticket type ID is required")
    @Positive(message = "Ticket type ID must be positive")
    private Long typeId;

    @PositiveOrZero(message = "Order cannot be negative")
    @Builder.Default
    private Integer order = 0;

    @NotNull(message = "Priority ID is required")
    @Positive(message = "Priority ID must be positive")
    private Long priorityId;

    @PositiveOrZero(message = "Estimation cannot be negative")
    @Builder.Default
    private BigDecimal estimation = BigDecimal.ZERO;

    @Positive(message = "Epic ID must be positive")
    private Long epicId;

    @Positive(message = "Parent ticket ID must be positive")
    private Long parentId;

    @Positive(message = "Sprint ID must be positive")
    private Long sprintId;

    @Positive(message = "Milestone ID must be positive")
    private Long milestoneId;

    private Set<@NotNull(message = "Label IDs cannot contain null") @Positive(message = "Label IDs must be positive") Long> labelIds;

    /** Dynamic values defined by the project/issue-type field configuration. */
    private Map<String, String> customFields;

    private IssueSecurityLevel securityLevel;
}
