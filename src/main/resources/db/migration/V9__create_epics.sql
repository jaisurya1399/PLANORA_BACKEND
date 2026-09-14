CREATE TABLE epics (
    id BIGSERIAL PRIMARY KEY,

    project_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,

    starts_at DATE NOT NULL,

    ends_at DATE NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    deleted_at TIMESTAMP NULL,

    parent_id BIGINT NULL,

    CONSTRAINT fk_epics_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT fk_epics_parent
        FOREIGN KEY (parent_id)
        REFERENCES epics(id)
);

CREATE INDEX idx_epics_project_id
    ON epics(project_id);

CREATE INDEX idx_epics_parent_id
    ON epics(parent_id);

CREATE INDEX idx_epics_deleted_at
    ON epics(deleted_at);