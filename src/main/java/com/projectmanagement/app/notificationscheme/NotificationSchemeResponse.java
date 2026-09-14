package com.projectmanagement.app.notificationscheme;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationSchemeResponse {
    Long id;
    Long projectId;
    String name;
    boolean enabled;
    List<NotificationSchemeRule> rules;
}
