CREATE TABLE project_users (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    project_id BIGINT NOT NULL,

    role VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_project_users_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_project_users_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
);

CREATE INDEX idx_project_users_user_id
    ON project_users(user_id);

CREATE INDEX idx_project_users_project_id
    ON project_users(project_id);


CREATE TABLE project_favorites (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    project_id BIGINT NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_project_favorites_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_project_favorites_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT project_favorites_user_project_unique
        UNIQUE (user_id, project_id)
);

CREATE INDEX idx_project_favorites_user_id
    ON project_favorites(user_id);

CREATE INDEX idx_project_favorites_project_id
    ON project_favorites(project_id);