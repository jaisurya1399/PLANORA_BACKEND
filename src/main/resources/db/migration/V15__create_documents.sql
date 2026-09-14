CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    original_name VARCHAR(500) NOT NULL,

    content_type VARCHAR(255) NOT NULL,

    file_size BIGINT NOT NULL,

    file_data BYTEA NOT NULL,

    ticket_id BIGINT NULL,

    uploaded_by BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_documents_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_documents_uploaded_by
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
);

CREATE INDEX idx_documents_ticket_id
    ON documents(ticket_id);

CREATE INDEX idx_documents_uploaded_by
    ON documents(uploaded_by);

CREATE INDEX idx_documents_created_at
    ON documents(created_at);