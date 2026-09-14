CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    guard_name VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT roles_name_guard_unique
        UNIQUE (name, guard_name)
);