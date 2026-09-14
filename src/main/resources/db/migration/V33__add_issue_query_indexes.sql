-- Supports the project issue list, board, backlog, and common filters.
CREATE INDEX idx_tickets_project_active_created
    ON tickets(project_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tickets_project_responsible_active
    ON tickets(project_id, responsible_id, "order", id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tickets_project_priority_active
    ON tickets(project_id, priority_id, "order", id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tickets_project_epic_active
    ON tickets(project_id, epic_id, "order", id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_ticket_labels_label_ticket ON ticket_labels(label_id, ticket_id);
