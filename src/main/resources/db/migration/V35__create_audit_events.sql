CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    ticket_id BIGINT NULL,
    actor_id BIGINT NULL,
    event_type VARCHAR(60) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id BIGINT NOT NULL,
    changes_json TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT audit_events_project_id_foreign FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT audit_events_ticket_id_foreign FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE SET NULL,
    CONSTRAINT audit_events_actor_id_foreign FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_audit_events_project_created ON audit_events(project_id, created_at DESC);
CREATE INDEX idx_audit_events_ticket_created ON audit_events(ticket_id, created_at DESC);
CREATE INDEX idx_audit_events_actor_created ON audit_events(actor_id, created_at DESC);
