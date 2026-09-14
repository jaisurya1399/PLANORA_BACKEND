-- Ticket codes are system-generated and must be globally unique.
-- Existing installations get a database-level uniqueness guarantee.
CREATE UNIQUE INDEX IF NOT EXISTS uk_tickets_code
    ON tickets(code);
