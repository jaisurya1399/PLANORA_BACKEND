package com.projectmanagement.app.automation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationEventRequest {
    Long projectId;
    Long ticketId;
    String eventType;
    String message;
}
