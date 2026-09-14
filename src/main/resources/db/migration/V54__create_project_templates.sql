-- ============================================================
-- V54: Project templates
-- ============================================================

CREATE TABLE project_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    ticket_prefix VARCHAR(255) NOT NULL,
    status_id BIGINT NOT NULL,
    status_type VARCHAR(255) NOT NULL DEFAULT 'default',
    created_by_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_project_templates_name_creator
        UNIQUE (name, created_by_id),
    CONSTRAINT fk_project_templates_status
        FOREIGN KEY (status_id) REFERENCES project_statuses(id),
    CONSTRAINT fk_project_templates_creator
        FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE INDEX idx_project_templates_creator
    ON project_templates(created_by_id);

CREATE INDEX idx_project_templates_status
    ON project_templates(status_id);
