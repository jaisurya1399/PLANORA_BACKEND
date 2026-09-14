ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS due_date TIMESTAMP NULL;

ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS overdue_notified_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_tickets_due_date
    ON tickets(due_date);

CREATE INDEX IF NOT EXISTS idx_tickets_overdue_notification
    ON tickets(due_date, overdue_notified_at);
