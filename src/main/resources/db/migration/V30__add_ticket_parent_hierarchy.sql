ALTER TABLE tickets
    ADD COLUMN parent_id BIGINT NULL,
    ADD CONSTRAINT tickets_parent_id_foreign
        FOREIGN KEY (parent_id) REFERENCES tickets(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_tickets_parent_not_self CHECK (parent_id IS NULL OR parent_id <> id);

CREATE INDEX idx_tickets_parent_id ON tickets(parent_id);
CREATE INDEX idx_tickets_project_parent_order_active
    ON tickets(project_id, parent_id, "order", id)
    WHERE deleted_at IS NULL;
