package com.projectmanagement.app.project;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Single source of truth for project-role permissions used by the API and the
 * frontend auth payload. Role definitions are global; the resulting
 * permissions are evaluated in the current project context.
 */
public final class ProjectPermissionCatalog {
    private ProjectPermissionCatalog() {
    }

    public static List<String> forRole(String role) {
        if (role == null)
            return List.of();
        return switch (role.trim().toUpperCase(Locale.ROOT)) {
            case "PROJECT_ADMIN" -> List.of(
                    "PROJECT_VIEW", "PROJECT_SETTINGS_MANAGE", "PROJECT_UPDATE", "PROJECT_MEMBER_MANAGE",
                    "PROJECT_ROLE_MANAGE",
                    "PROJECT_WORKFLOW_MANAGE", "PROJECT_BOARD_MANAGE", "PROJECT_REPORT_VIEW", "PROJECT_AUDIT_VIEW",
                    "PROJECT_STATUS_VIEW", "PROJECT_STATUS_CREATE", "PROJECT_STATUS_UPDATE", "PROJECT_STATUS_DELETE",
                    "TICKET_CREATE", "TICKET_VIEW", "TICKET_EDIT", "TICKET_ASSIGN", "TICKET_STATUS_CHANGE",
                    "TICKET_DELETE",
                    "BACKLOG_MANAGE", "SPRINT_MANAGE", "SPRINT_ANALYTICS_VIEW", "COMMENT_CREATE", "ATTACHMENT_UPLOAD",
                    "WORKLOG_CREATE", "PROJECT_TEAM_VIEW", "TICKET_TYPE_MANAGE", "TICKET_STATUS_MANAGE",
                    "TICKET_PRIORITY_MANAGE", "NOTIFICATION_MANAGE", "AUTOMATION_MANAGE", "DAILY_SCRUM_VIEW",
                    "DAILY_SCRUM_UPDATE", "NOTIFICATION_VIEW");
            case "TEAM_LEAD" -> List.of(
                    "PROJECT_VIEW", "TICKET_CREATE", "TICKET_VIEW", "TICKET_EDIT", "TICKET_ASSIGN",
                    "TICKET_STATUS_CHANGE",
                    "BACKLOG_MANAGE", "SPRINT_MANAGE", "SPRINT_ANALYTICS_VIEW", "COMMENT_CREATE", "ATTACHMENT_UPLOAD",
                    "WORKLOG_CREATE", "PROJECT_REPORT_VIEW", "PROJECT_TEAM_VIEW", "DAILY_SCRUM_VIEW",
                    "DAILY_SCRUM_UPDATE", "NOTIFICATION_VIEW");
            case "DEVELOPER", "MEMBER" -> List.of(
                    "PROJECT_VIEW", "TICKET_CREATE", "TICKET_VIEW", "TICKET_EDIT", "TICKET_STATUS_CHANGE",
                    "COMMENT_CREATE", "ATTACHMENT_UPLOAD", "WORKLOG_CREATE", "DAILY_SCRUM_VIEW", "DAILY_SCRUM_UPDATE",
                    "NOTIFICATION_VIEW");
            case "VIEWER" -> List.of("PROJECT_VIEW", "TICKET_VIEW", "PROJECT_REPORT_VIEW");
            default -> List.of();
        };
    }

    /**
     * Convert canonical frontend/API permission names to legacy Spring authority
     * names.
     */
    public static String toAuthority(String permission) {
        if (permission == null || permission.isBlank())
            return permission;
        String p = permission.trim().toUpperCase(Locale.ROOT);
        return switch (p) {
            case "PROJECT_VIEW" -> "project.view";
            case "PROJECT_SETTINGS_MANAGE" -> "project.settings.manage";
            case "PROJECT_UPDATE" -> "project.update";
            case "PROJECT_MEMBER_MANAGE" -> "project.member.manage";
            case "PROJECT_ROLE_MANAGE" -> "project.role.manage";
            case "PROJECT_WORKFLOW_MANAGE" -> "project.workflow.manage";
            case "PROJECT_STATUS_VIEW" -> "project_status.view";
            case "PROJECT_STATUS_CREATE" -> "project_status.create";
            case "PROJECT_STATUS_UPDATE" -> "project_status.update";
            case "PROJECT_STATUS_DELETE" -> "project_status.delete";
            case "PROJECT_BOARD_MANAGE" -> "project.board.manage";
            case "PROJECT_REPORT_VIEW" -> "project.report.view";
            case "PROJECT_AUDIT_VIEW" -> "project.audit.view";
            case "TICKET_CREATE" -> "ticket.create";
            case "TICKET_VIEW" -> "ticket.view";
            case "TICKET_EDIT" -> "ticket.update";
            case "TICKET_ASSIGN" -> "ticket.assign";
            case "TICKET_STATUS_CHANGE" -> "ticket.status.change";
            case "TICKET_DELETE" -> "ticket.delete";
            case "BACKLOG_MANAGE" -> "backlog.manage";
            case "SPRINT_MANAGE" -> "sprint.manage";
            case "SPRINT_ANALYTICS_VIEW" -> "sprint.analytics.view";
            case "COMMENT_CREATE" -> "ticket_comment.create";
            case "ATTACHMENT_UPLOAD" -> "ticket_attachment.create";
            case "WORKLOG_CREATE" -> "timesheet.create";
            case "PROJECT_TEAM_VIEW" -> "project_team.view";
            case "TICKET_TYPE_MANAGE" -> "ticket_type.create";
            case "TICKET_STATUS_MANAGE" -> "ticket_status.create";
            case "TICKET_PRIORITY_MANAGE" -> "ticket_priority.create";
            case "NOTIFICATION_MANAGE" -> "notification.update";
            case "AUTOMATION_MANAGE" -> "automation.update";
            case "DAILY_SCRUM_VIEW" -> "daily_scrum.view";
            case "DAILY_SCRUM_UPDATE" -> "daily_scrum.update";
            case "NOTIFICATION_VIEW" -> "notification.view";
            default -> permission;
        };
    }

    public static Set<String> authoritiesForRole(String role) {
        Set<String> result = new LinkedHashSet<>();
        for (String permission : forRole(role))
            result.add(toAuthority(permission));

        String r = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if ("PROJECT_ADMIN".equals(r)) {
            addCrud(result, "project_status");
            addCrud(result, "project_team");
            addCrud(result, "epic");
            addCrud(result, "release");
            addCrud(result, "workflow");
            addCrud(result, "screen_configuration");
            addCrud(result, "field_configuration");
            addCrud(result, "custom_field");
            addCrud(result, "ticket_type");
            addCrud(result, "ticket_status");
            addCrud(result, "ticket_priority");
            addCrud(result, "ticket_template");
            addCrud(result, "ticket");
            addCrud(result, "ticket_activity");
            addCrud(result, "ticket_attachment");
            addCrud(result, "ticket_comment");
            addCrud(result, "ticket_hour");
            addCrud(result, "ticket_relation");
            addCrud(result, "ticket_subscriber");
            addCrud(result, "timesheet");
            addCrud(result, "timesheet_cell");
            addCrud(result, "daily_scrum");
            addCrud(result, "notification");
            addCrud(result, "project_favorite");
            addCrud(result, "ticket_saved_view");
            result.add("project.update");
            result.add("project.delete");
            result.add("permission_scheme.view");
            result.add("permission_scheme.update");
            result.add("issue_security.view");
            result.add("issue_security.update");
            result.add("priority_scheme.view");
            result.add("priority_scheme.update");
        } else if ("TEAM_LEAD".equals(r) || "DEVELOPER".equals(r) || "MEMBER".equals(r)) {
            addCrudViewCreateUpdate(result, "epic");
            addCrudViewCreateUpdate(result, "release");
            result.add("project_team.view");
            addCrudViewCreateUpdate(result, "ticket");
            addCrudViewCreateUpdate(result, "ticket_activity");
            addCrudViewCreateUpdate(result, "ticket_attachment");
            addCrudViewCreateUpdate(result, "ticket_comment");
            addCrudViewCreateUpdate(result, "ticket_hour");
            addCrudViewCreateUpdate(result, "ticket_relation");
            addCrudViewCreateUpdate(result, "ticket_subscriber");
            addCrudViewCreateUpdate(result, "timesheet");
            addCrudViewCreateUpdate(result, "timesheet_cell");
            result.add("daily_scrum.view");
            result.add("daily_scrum.update");
            result.add("notification.view");
        }
        return result;
    }

    private static void addCrud(Set<String> set, String prefix) {
        set.add(prefix + ".view");
        set.add(prefix + ".create");
        set.add(prefix + ".update");
        set.add(prefix + ".delete");
    }

    private static void addCrudViewCreateUpdate(Set<String> set, String prefix) {
        set.add(prefix + ".view");
        set.add(prefix + ".create");
        set.add(prefix + ".update");
    }

}
