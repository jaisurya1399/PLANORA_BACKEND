-- Keep controller and database permission names consistent for non-ADMIN roles.
INSERT INTO permissions(name,guard_name,created_at,updated_at) VALUES
('project_user.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('project_user.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('project_user.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('timesheet.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('timesheet.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('timesheet.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('timesheet.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('timesheet_cell.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('timesheet_cell.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('timesheet_cell.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('timesheet_cell.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('ticket_relation.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('user_group.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('user_group.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('user_group.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('user_group.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT(name,guard_name) DO NOTHING;
-- Backfill ADMIN with all newly synchronized permissions.
INSERT INTO role_has_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.name='ADMIN' AND p.name IN ('project_user.create','project_user.update','project_user.delete','timesheet.view','timesheet.create','timesheet.update','timesheet.delete','timesheet_cell.view','timesheet_cell.create','timesheet_cell.update','timesheet_cell.delete','ticket_relation.update','user_group.view','user_group.create','user_group.update','user_group.delete')
AND NOT EXISTS(SELECT 1 FROM role_has_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
