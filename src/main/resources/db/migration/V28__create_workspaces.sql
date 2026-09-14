CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description TEXT NULL,
    created_by BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT uk_workspaces_slug UNIQUE (slug),
    CONSTRAINT workspaces_created_by_foreign
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE workspace_members (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    CONSTRAINT workspace_members_workspace_id_foreign
        FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT workspace_members_user_id_foreign
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_workspace_members_workspace_user UNIQUE (workspace_id, user_id),
    CONSTRAINT chk_workspace_members_role
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'GUEST'))
);

CREATE INDEX idx_workspace_members_user_id ON workspace_members(user_id);
CREATE INDEX idx_workspace_members_workspace_id ON workspace_members(workspace_id);

-- Existing single-tenant data is retained in one default workspace.
INSERT INTO workspaces (name, slug, description, created_at)
SELECT 'Default workspace', 'default', 'Workspace created while migrating existing projects', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM workspaces WHERE slug = 'default');

INSERT INTO workspace_members (workspace_id, user_id, role, created_at)
SELECT w.id, u.id, 'MEMBER', CURRENT_TIMESTAMP
FROM workspaces w
CROSS JOIN users u
WHERE w.slug = 'default'
  AND u.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM workspace_members wm
      WHERE wm.workspace_id = w.id AND wm.user_id = u.id
  );

ALTER TABLE projects ADD COLUMN workspace_id BIGINT;

UPDATE projects
SET workspace_id = (SELECT id FROM workspaces WHERE slug = 'default')
WHERE workspace_id IS NULL;

ALTER TABLE projects
    ALTER COLUMN workspace_id SET NOT NULL,
    ADD CONSTRAINT projects_workspace_id_foreign
        FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

CREATE INDEX idx_projects_workspace_id ON projects(workspace_id);
CREATE INDEX idx_projects_workspace_deleted_at ON projects(workspace_id, deleted_at);
