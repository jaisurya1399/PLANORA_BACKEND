-- ============================================================
-- V53: Project archive + template/clone permissions
-- ============================================================

ALTER TABLE projects
    ADD COLUMN archived_at TIMESTAMP NULL;

CREATE INDEX idx_projects_archived_at
    ON projects(archived_at);

INSERT INTO permissions (name, guard_name, created_at, updated_at)
VALUES
    ('project.archive', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.restore_archive', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.audit.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.settings.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.settings.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project.clone', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_template.view', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_template.create', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_template.update', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('project_template.delete', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name, guard_name) DO NOTHING;
