CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    content TEXT NOT NULL,

    owner_id BIGINT NOT NULL,

    responsible_id BIGINT NULL,

    status_id BIGINT NOT NULL,

    project_id BIGINT NOT NULL,

    deleted_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    code VARCHAR(255) NOT NULL,

    type_id BIGINT NOT NULL,

    "order" INTEGER NOT NULL DEFAULT 0,

    priority_id BIGINT NOT NULL,

    estimation NUMERIC(8,2),

    epic_id BIGINT NULL,

    CONSTRAINT fk_tickets_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id),

    CONSTRAINT fk_tickets_responsible
        FOREIGN KEY (responsible_id)
        REFERENCES users(id),

    CONSTRAINT fk_tickets_status
        FOREIGN KEY (status_id)
        REFERENCES ticket_statuses(id),

    CONSTRAINT fk_tickets_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT fk_tickets_type
        FOREIGN KEY (type_id)
        REFERENCES ticket_types(id),

    CONSTRAINT fk_tickets_priority
        FOREIGN KEY (priority_id)
        REFERENCES ticket_priorities(id),

    CONSTRAINT fk_tickets_epic
        FOREIGN KEY (epic_id)
        REFERENCES epics(id)
);

CREATE INDEX idx_tickets_owner_id
    ON tickets(owner_id);

CREATE INDEX idx_tickets_responsible_id
    ON tickets(responsible_id);

CREATE INDEX idx_tickets_status_id
    ON tickets(status_id);

CREATE INDEX idx_tickets_project_id
    ON tickets(project_id);

CREATE INDEX idx_tickets_type_id
    ON tickets(type_id);

CREATE INDEX idx_tickets_priority_id
    ON tickets(priority_id);

CREATE INDEX idx_tickets_epic_id
    ON tickets(epic_id);

CREATE INDEX idx_tickets_deleted_at
    ON tickets(deleted_at);

CREATE INDEX idx_tickets_project_code
    ON tickets(project_id, code);