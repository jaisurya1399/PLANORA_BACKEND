CREATE TABLE project_statuses (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    color VARCHAR(255) NOT NULL DEFAULT '#cecece',

    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    deleted_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_project_statuses_deleted_at
    ON project_statuses(deleted_at);