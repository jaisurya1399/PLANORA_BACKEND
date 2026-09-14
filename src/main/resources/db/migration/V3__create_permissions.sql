CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    guard_name VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT permissions_name_guard_unique
        UNIQUE (name, guard_name)
);