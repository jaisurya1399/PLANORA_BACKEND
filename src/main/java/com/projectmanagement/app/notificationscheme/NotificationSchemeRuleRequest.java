package com.projectmanagement.app.notificationscheme;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSchemeRuleRequest {
    String eventType;
    String recipientType;
    Boolean inAppEnabled;
    Boolean emailEnabled;
}
