-- ============================================================
-- V74: Tighten System Admin scope
--
-- System Admin is a platform role. It may manage users, global
-- roles/permissions, security, enterprise configuration, and the
-- project lifecycle. It must not receive project-operational
-- permissions such as tickets, backlog, sprint, project members,
-- workflow, board, time tracking, etc.
-- ============================================================

DELETE FROM role_has_permissions rp
USING roles r, permissions p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND r.name = 'ADMIN'
  AND r.guard_name = 'web'
  AND p.name IN (
    'project.member.manage',
    'project.role.manage',
    'project.workflow.manage',
    'project.board.manage',
    'project.report.view',
    'project.audit.view',
    'ticket.create',
    'ticket.view',
    'ticket.edit',
    'ticket.update',
    'ticket.assign',
    'ticket.status.change',
    'ticket.delete',
    'backlog.manage',
    'sprint.manage',
    'sprint.analytics.view',
    'comment.create',
    'attachment.upload',
    'worklog.create',
    'project.team.view',
    'ticket.type.manage',
    'ticket.status.manage',
    'ticket.priority.manage',
    'notification.manage',
    'automation.manage',
    'project_status.view',
    'project_status.create',
    'project_status.update',
    'project_status.delete',
    'ticket_type.view',
    'ticket_type.create',
    'ticket_type.update',
    'ticket_type.delete',
    'ticket_priority.view',
    'ticket_priority.create',
    'ticket_priority.update',
    'ticket_priority.delete',
    'ticket_status.view',
    'ticket_status.create',
    'ticket_status.update',
    'ticket_status.delete',
    'epic.view',
    'epic.create',
    'epic.update',
    'epic.delete',
    'release.view',
    'release.create',
    'release.update',
    'release.delete',
    'project_team.view',
    'project_team.create',
    'project_team.update',
    'project_team.delete',
    'timesheet.view',
    'timesheet.create',
    'timesheet.update',
    'timesheet.delete',
    'time_sheet.view',
    'time_sheet.create',
    'time_sheet.update',
    'time_sheet.delete',
    'time_sheet_cell.view',
    'time_sheet_cell.create',
    'time_sheet_cell.update',
    'time_sheet_cell.delete',
    'ticket_activity.view',
    'ticket_activity.create',
    'ticket_activity.delete',
    'ticket_comment.create',
    'ticket_comment.update',
    'ticket_comment.delete'
  );
