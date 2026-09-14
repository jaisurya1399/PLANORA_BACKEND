-- ============================================================
-- V55: Give system ADMIN all project-management permissions
-- ============================================================

INSERT INTO role_has_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND r.guard_name = 'web'
  AND p.name IN (
    'project.archive',
    'project.restore_archive',
    'project.audit.view',
    'project.settings.view',
    'project.settings.update',
    'project.clone',
    'project_template.view',
    'project_template.create',
    'project_template.update',
    'project_template.delete'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_has_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
