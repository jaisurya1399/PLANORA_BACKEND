-- Keep issue data valid even when it is changed outside the application.
UPDATE tickets
SET estimation = 0
WHERE estimation IS NULL;

ALTER TABLE tickets
    ALTER COLUMN estimation SET DEFAULT 0,
    ALTER COLUMN estimation SET NOT NULL;

ALTER TABLE tickets
    ADD CONSTRAINT chk_tickets_order_non_negative CHECK ("order" >= 0),
    ADD CONSTRAINT chk_tickets_estimation_non_negative CHECK (estimation >= 0);

-- Codes are the stable issue identifiers used in URLs, integrations and references.
CREATE UNIQUE INDEX uk_tickets_code ON tickets (code);

CREATE INDEX idx_tickets_project_status_order_active
    ON tickets (project_id, status_id, "order", id)
    WHERE deleted_at IS NULL;
