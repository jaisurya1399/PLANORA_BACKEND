ALTER TABLE project_users
ADD COLUMN IF NOT EXISTS availability_self_update_open BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_project_users_availability_open
ON project_users (availability_self_update_open);