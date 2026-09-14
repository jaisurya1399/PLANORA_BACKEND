CREATE TABLE board_configs (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    swimlane_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    enforce_wip BOOLEAN NOT NULL DEFAULT FALSE,
    active_sprint_only BOOLEAN NOT NULL DEFAULT TRUE,
    show_epic BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NULL,
    CONSTRAINT uk_board_configs_project UNIQUE (project_id),
    CONSTRAINT board_configs_project_id_foreign FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE board_columns (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    wip_limit INTEGER NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT uk_board_columns_project_status UNIQUE (project_id, status_id),
    CONSTRAINT board_columns_project_id_foreign FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT board_columns_status_id_foreign FOREIGN KEY (status_id) REFERENCES ticket_statuses(id) ON DELETE CASCADE,
    CONSTRAINT board_columns_wip_limit_check CHECK (wip_limit IS NULL OR wip_limit >= 0)
);
CREATE INDEX idx_board_columns_project_order ON board_columns(project_id, display_order);

CREATE TABLE board_status_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    from_status_id BIGINT NULL,
    to_status_id BIGINT NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT board_status_history_project_id_foreign FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT board_status_history_ticket_id_foreign FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT board_status_history_from_status_foreign FOREIGN KEY (from_status_id) REFERENCES ticket_statuses(id) ON DELETE SET NULL,
    CONSTRAINT board_status_history_to_status_foreign FOREIGN KEY (to_status_id) REFERENCES ticket_statuses(id) ON DELETE RESTRICT
);
CREATE INDEX idx_board_status_history_project_time ON board_status_history(project_id, changed_at);
CREATE INDEX idx_board_status_history_ticket_time ON board_status_history(ticket_id, changed_at);
