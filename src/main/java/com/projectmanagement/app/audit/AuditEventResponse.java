package com.projectmanagement.app.audit;

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
public class AuditEventResponse {
    private Long id;
    private Long projectId;
    private Long ticketId;
    private Long actorId;
    private String actorName;
    private String eventType;
    private String entityType;
    private Long entityId;
    private String changesJson;
    private LocalDateTime createdAt;
}
