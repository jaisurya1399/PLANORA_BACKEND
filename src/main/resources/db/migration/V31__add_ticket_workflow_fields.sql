ALTER TABLE ticket_statuses
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'TODO',
    ADD CONSTRAINT chk_ticket_statuses_category
        CHECK (category IN ('BACKLOG', 'TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'));

UPDATE ticket_statuses
SET category = CASE
    WHEN LOWER(TRIM(name)) IN ('backlog', 'icebox') THEN 'BACKLOG'
    WHEN LOWER(TRIM(name)) IN ('done', 'completed', 'closed', 'resolved') THEN 'DONE'
    WHEN LOWER(TRIM(name)) IN ('cancelled', 'canceled', 'wontfix', 'won''t fix') THEN 'CANCELLED'
    WHEN LOWER(TRIM(name)) IN ('in progress', 'in-progress', 'in review', 'review', 'testing') THEN 'IN_PROGRESS'
    ELSE 'TODO'
END;

ALTER TABLE tickets ADD COLUMN resolved_at TIMESTAMP NULL;

UPDATE tickets t
SET resolved_at = t.updated_at
FROM ticket_statuses s
WHERE t.status_id = s.id
  AND s.category IN ('DONE', 'CANCELLED');

CREATE INDEX idx_ticket_statuses_project_category_order
    ON ticket_statuses(project_id, category, "order");
CREATE INDEX idx_tickets_resolved_at ON tickets(resolved_at);
