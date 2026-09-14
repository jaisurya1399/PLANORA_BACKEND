-- ============================================================
-- Custom Fields / Field Configuration / Screen Configuration
-- ============================================================

CREATE TABLE custom_fields (
    id BIGSERIAL PRIMARY KEY,
    field_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    description TEXT NULL,
    options_json TEXT NULL,
    required_by_default BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT chk_custom_field_type CHECK (type IN ('TEXT','TEXTAREA','NUMBER','DATE','DATETIME','BOOLEAN','SELECT','MULTI_SELECT','URL'))
);
CREATE INDEX idx_custom_fields_active ON custom_fields(active);

CREATE TABLE field_configurations (
    id BIGSERIAL PRIMARY KEY,
    field_id BIGINT NOT NULL,
    project_id BIGINT NULL,
    ticket_type_id BIGINT NULL,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_field_config_field FOREIGN KEY (field_id) REFERENCES custom_fields(id) ON DELETE CASCADE,
    CONSTRAINT fk_field_config_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_field_config_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id) ON DELETE CASCADE,
    CONSTRAINT uq_field_configuration UNIQUE (field_id, project_id, ticket_type_id)
);
CREATE INDEX idx_field_config_project_type ON field_configurations(project_id, ticket_type_id);

CREATE TABLE screen_configurations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    project_id BIGINT NULL,
    ticket_type_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_screen_config_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_screen_config_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id) ON DELETE CASCADE
);
CREATE INDEX idx_screen_config_project_type ON screen_configurations(project_id, ticket_type_id);

CREATE TABLE screen_fields (
    id BIGSERIAL PRIMARY KEY,
    screen_id BIGINT NOT NULL,
    field_id BIGINT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NULL,
    CONSTRAINT fk_screen_field_screen FOREIGN KEY (screen_id) REFERENCES screen_configurations(id) ON DELETE CASCADE,
    CONSTRAINT fk_screen_field_field FOREIGN KEY (field_id) REFERENCES custom_fields(id) ON DELETE CASCADE,
    CONSTRAINT uq_screen_field UNIQUE (screen_id, field_id)
);
CREATE INDEX idx_screen_fields_screen ON screen_fields(screen_id, display_order);

CREATE TABLE custom_field_values (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    field_id BIGINT NOT NULL,
    value TEXT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_custom_value_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_custom_value_field FOREIGN KEY (field_id) REFERENCES custom_fields(id) ON DELETE CASCADE,
    CONSTRAINT uq_custom_field_value UNIQUE (ticket_id, field_id)
);
CREATE INDEX idx_custom_field_values_ticket ON custom_field_values(ticket_id);
CREATE INDEX idx_custom_field_values_field ON custom_field_values(field_id);

-- Default global screen for the standard issue form.
INSERT INTO screen_configurations (name, project_id, ticket_type_id, active, created_at, updated_at)
SELECT 'Default Issue Screen', NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM screen_configurations WHERE name = 'Default Issue Screen' AND project_id IS NULL AND ticket_type_id IS NULL);
