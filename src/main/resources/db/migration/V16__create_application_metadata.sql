CREATE TABLE application_metadata (
    id BIGSERIAL PRIMARY KEY,

    metadata_key VARCHAR(255) NOT NULL,

    metadata_value TEXT,

    description TEXT,

    created_at TIMESTAMP,

    updated_at TIMESTAMP,

    CONSTRAINT application_metadata_key_unique
        UNIQUE (metadata_key)
);

INSERT INTO application_metadata (
    metadata_key,
    metadata_value,
    description,
    created_at,
    updated_at
)
VALUES (
    'application_version',
    '1.0.0',
    'Project Management application version',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);