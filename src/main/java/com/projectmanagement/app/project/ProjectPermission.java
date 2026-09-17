package com.projectmanagement.app.project;

/**
 * Canonical project-scoped permissions used by Planora's project RBAC model.
 * Global ADMIN permissions remain separate from these project permissions.
 */
public enum ProjectPermission {
    PROJECT_VIEW,
    PROJECT_SETTINGS_MANAGE,
    PROJECT_MEMBER_MANAGE,
    PROJECT_ROLE_MANAGE,
    PROJECT_WORKFLOW_MANAGE,
    PROJECT_BOARD_MANAGE,
    PROJECT_REPORT_VIEW,
    PROJECT_AUDIT_VIEW,
    TICKET_CREATE,
    TICKET_VIEW,
    TICKET_EDIT,
    TICKET_ASSIGN,
    TICKET_STATUS_CHANGE,
    TICKET_DELETE,
    BACKLOG_MANAGE,
    SPRINT_MANAGE,
    SPRINT_ANALYTICS_VIEW,
    COMMENT_CREATE,
    ATTACHMENT_UPLOAD,
    WORKLOG_CREATE,
    PROJECT_TEAM_VIEW,
    TICKET_TYPE_MANAGE,
    TICKET_STATUS_MANAGE,
    TICKET_PRIORITY_MANAGE,
    NOTIFICATION_MANAGE,
    AUTOMATION_MANAGE
}
