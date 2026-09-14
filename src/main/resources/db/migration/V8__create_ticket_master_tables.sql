CREATE TABLE ticket_types (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    icon VARCHAR(255) NOT NULL,

    color VARCHAR(255) NOT NULL DEFAULT '#cecece',

    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    deleted_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_ticket_types_deleted_at
    ON ticket_types(deleted_at);


CREATE TABLE ticket_priorities (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    color VARCHAR(255) NOT NULL DEFAULT '#cecece',

    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    deleted_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_ticket_priorities_deleted_at
    ON ticket_priorities(deleted_at);


CREATE TABLE ticket_statuses (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    color VARCHAR(255) NOT NULL DEFAULT '#cecece',

    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    deleted_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    "order" INTEGER NOT NULL DEFAULT 1,

    project_id BIGINT NULL,

    CONSTRAINT fk_ticket_statuses_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
);

CREATE INDEX idx_ticket_statuses_project_id
    ON ticket_statuses(project_id);

CREATE INDEX idx_ticket_statuses_deleted_at
    ON ticket_statuses(deleted_at);