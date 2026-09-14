CREATE TABLE IF NOT EXISTS custom_dashboards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    project_id BIGINT NULL,
    widgets_json TEXT NOT NULL DEFAULT '[]',
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_custom_dashboards_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_custom_dashboards_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_custom_dashboards_user ON custom_dashboards(user_id);
CREATE INDEX IF NOT EXISTS idx_custom_dashboards_project ON custom_dashboards(project_id);
