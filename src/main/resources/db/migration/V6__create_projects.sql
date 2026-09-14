CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    description TEXT NULL,

    owner_id BIGINT NOT NULL,

    status_id BIGINT NOT NULL,

    deleted_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    ticket_prefix VARCHAR(255) NOT NULL,

    status_type VARCHAR(255) NOT NULL DEFAULT 'default',

    CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id),

    CONSTRAINT fk_projects_status
        FOREIGN KEY (status_id)
        REFERENCES project_statuses(id)
);

CREATE INDEX idx_projects_owner_id
    ON projects(owner_id);

CREATE INDEX idx_projects_status_id
    ON projects(status_id);

CREATE INDEX idx_projects_deleted_at
    ON projects(deleted_at);