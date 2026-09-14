CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    email VARCHAR(255) NOT NULL,

    email_verified_at TIMESTAMP NULL,

    password VARCHAR(255) NULL,

    two_factor_secret TEXT NULL,

    two_factor_recovery_codes TEXT NULL,

    two_factor_confirmed_at TIMESTAMP NULL,

    remember_token VARCHAR(100) NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    deleted_at TIMESTAMP NULL,

    creation_token UUID NULL,

    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_users_deleted_at
    ON users(deleted_at);