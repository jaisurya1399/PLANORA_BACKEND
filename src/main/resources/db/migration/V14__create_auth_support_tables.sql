CREATE TABLE password_resets (
    email VARCHAR(255) NOT NULL,

    token VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NULL
);

CREATE INDEX idx_password_resets_email
    ON password_resets(email);


CREATE TABLE pending_user_emails (
    id BIGSERIAL PRIMARY KEY,

    user_type VARCHAR(255) NOT NULL,

    user_id BIGINT NOT NULL,

    email VARCHAR(255) NOT NULL,

    token VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NULL
);

CREATE INDEX idx_pending_user_emails_user
    ON pending_user_emails(user_type, user_id);

CREATE INDEX idx_pending_user_emails_email
    ON pending_user_emails(email);


CREATE TABLE socialite_users (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    provider VARCHAR(255) NOT NULL,

    provider_id VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_socialite_users_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT socialite_provider_provider_id_unique
        UNIQUE (provider, provider_id)
);


CREATE TABLE personal_access_tokens (
    id BIGSERIAL PRIMARY KEY,

    tokenable_type VARCHAR(255) NOT NULL,

    tokenable_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,

    token VARCHAR(64) NOT NULL,

    abilities TEXT NULL,

    last_used_at TIMESTAMP NULL,

    expires_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT personal_access_tokens_token_unique
        UNIQUE (token)
);

CREATE INDEX idx_personal_access_tokens_tokenable
    ON personal_access_tokens(tokenable_type, tokenable_id);


CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,

    queue VARCHAR(255) NOT NULL,

    payload TEXT NOT NULL,

    attempts SMALLINT NOT NULL,

    reserved_at INTEGER NULL,

    available_at INTEGER NOT NULL,

    created_at INTEGER NOT NULL
);

CREATE INDEX idx_jobs_queue
    ON jobs(queue);


CREATE TABLE failed_jobs (
    id BIGSERIAL PRIMARY KEY,

    uuid VARCHAR(255) NOT NULL,

    connection TEXT NOT NULL,

    payload TEXT NOT NULL,

    exception TEXT NOT NULL,

    failed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT failed_jobs_uuid_unique
        UNIQUE (uuid)
);