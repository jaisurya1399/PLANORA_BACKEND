-- ============================================================
-- V17: Seed authorization permissions
-- ============================================================

INSERT INTO permissions (
    name,
    guard_name,
    created_at,
    updated_at
)
VALUES

    -- --------------------------------------------------------
    -- Users
    -- --------------------------------------------------------

    ('user.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Roles
    -- --------------------------------------------------------

    ('role.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('role.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('role.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('role.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Permissions
    -- --------------------------------------------------------

    ('permission.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('permission.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('permission.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('permission.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Role Permissions
    -- --------------------------------------------------------

    ('role_permission.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('role_permission.assign', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('role_permission.remove', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- User Roles
    -- --------------------------------------------------------

    ('user_role.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user_role.assign', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('user_role.remove', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Project Status
    -- --------------------------------------------------------

    ('project_status.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_status.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_status.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_status.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Projects
    -- --------------------------------------------------------

    ('project.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Project Users
    -- --------------------------------------------------------

    ('project_user.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_user.assign', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_user.remove', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Project Favorites
    -- --------------------------------------------------------

    ('project_favorite.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_favorite.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_favorite.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Types
    -- --------------------------------------------------------

    ('ticket_type.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_type.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_type.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_type.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Priorities
    -- --------------------------------------------------------

    ('ticket_priority.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_priority.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_priority.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_priority.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Statuses
    -- --------------------------------------------------------

    ('ticket_status.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_status.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_status.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_status.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Epics
    -- --------------------------------------------------------

    ('epic.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('epic.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('epic.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('epic.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Tickets
    -- --------------------------------------------------------

    ('ticket.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Comments
    -- --------------------------------------------------------

    ('ticket_comment.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_comment.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_comment.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_comment.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Subscribers
    -- --------------------------------------------------------

    ('ticket_subscriber.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_subscriber.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_subscriber.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Relations
    -- --------------------------------------------------------

    ('ticket_relation.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_relation.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_relation.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Activities
    -- --------------------------------------------------------

    ('ticket_activity.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_activity.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_activity.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Ticket Hours
    -- --------------------------------------------------------

    ('ticket_hour.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_hour.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_hour.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ticket_hour.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Time Sheets
    -- --------------------------------------------------------

    ('time_sheet.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('time_sheet.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('time_sheet.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('time_sheet.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Time Sheet Cells
    -- --------------------------------------------------------

    ('time_sheet_cell.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('time_sheet_cell.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('time_sheet_cell.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('time_sheet_cell.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Notifications
    -- --------------------------------------------------------

    ('notification.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notification.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notification.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notification.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Settings
    -- --------------------------------------------------------

    ('setting.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('setting.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('setting.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('setting.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Documents
    -- --------------------------------------------------------

    ('document.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('document.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('document.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('document.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- --------------------------------------------------------
    -- Application Metadata
    -- --------------------------------------------------------

    ('metadata.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('metadata.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('metadata.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('metadata.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON CONFLICT (name, guard_name) DO NOTHING;