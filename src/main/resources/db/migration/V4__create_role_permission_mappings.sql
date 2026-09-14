CREATE TABLE role_has_permissions (
    permission_id BIGINT NOT NULL,

    role_id BIGINT NOT NULL,

    PRIMARY KEY (permission_id, role_id),

    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_role_has_permissions_role_id
    ON role_has_permissions(role_id);


CREATE TABLE model_has_roles (
    role_id BIGINT NOT NULL,

    model_type VARCHAR(255) NOT NULL,

    model_id BIGINT NOT NULL,

    PRIMARY KEY (role_id, model_id, model_type),

    CONSTRAINT fk_model_has_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_model_has_roles_model
    ON model_has_roles(model_id, model_type);


CREATE TABLE model_has_permissions (
    permission_id BIGINT NOT NULL,

    model_type VARCHAR(255) NOT NULL,

    model_id BIGINT NOT NULL,

    PRIMARY KEY (permission_id, model_id, model_type),

    CONSTRAINT fk_model_has_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_model_has_permissions_model
    ON model_has_permissions(model_id, model_type);