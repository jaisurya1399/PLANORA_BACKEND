-- Removes the multi-organization Workspace layer.
-- The product is single-organization, multi-project: projects no longer
-- belong to a workspace, and workspace membership is no longer a concept.

ALTER TABLE projects DROP CONSTRAINT IF EXISTS projects_workspace_id_foreign;

DROP INDEX IF EXISTS idx_projects_workspace_id;
DROP INDEX IF EXISTS idx_projects_workspace_deleted_at;

ALTER TABLE projects DROP COLUMN IF EXISTS workspace_id;

DROP TABLE IF EXISTS workspace_members CASCADE;

DROP TABLE IF EXISTS workspaces CASCADE;
