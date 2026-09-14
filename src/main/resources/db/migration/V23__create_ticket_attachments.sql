CREATE TABLE ticket_attachments (
    id BIGSERIAL PRIMARY KEY,

    ticket_id BIGINT NOT NULL,

    file_name VARCHAR(255) NOT NULL,

    original_name VARCHAR(255) NOT NULL,

    content_type VARCHAR(255) NOT NULL,

    file_size BIGINT NOT NULL,

    file_data BYTEA NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT ticket_attachments_ticket_id_foreign
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ticket_attachments_ticket_id
    ON ticket_attachments(ticket_id);

CREATE INDEX idx_ticket_attachments_created_at
    ON ticket_attachments(created_at);