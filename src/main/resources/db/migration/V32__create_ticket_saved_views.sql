CREATE TABLE ticket_saved_views (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    query_text VARCHAR(255) NULL,
    status_id BIGINT NULL,
    priority_id BIGINT NULL,
    responsible_id BIGINT NULL,
    sprint_id BIGINT NULL,
    epic_id BIGINT NULL,
    label_id BIGINT NULL,
    root_only BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    CONSTRAINT ticket_saved_views_project_id_foreign FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT ticket_saved_views_owner_id_foreign FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ticket_saved_views_status_id_foreign FOREIGN KEY (status_id) REFERENCES ticket_statuses(id) ON DELETE SET NULL,
    CONSTRAINT ticket_saved_views_priority_id_foreign FOREIGN KEY (priority_id) REFERENCES ticket_priorities(id) ON DELETE SET NULL,
    CONSTRAINT ticket_saved_views_responsible_id_foreign FOREIGN KEY (responsible_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT ticket_saved_views_sprint_id_foreign FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE SET NULL,
    CONSTRAINT ticket_saved_views_epic_id_foreign FOREIGN KEY (epic_id) REFERENCES epics(id) ON DELETE SET NULL,
    CONSTRAINT ticket_saved_views_label_id_foreign FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE SET NULL,
    CONSTRAINT uk_ticket_saved_views_project_owner_name UNIQUE (project_id, owner_id, name)
);

CREATE INDEX idx_ticket_saved_views_project_owner ON ticket_saved_views(project_id, owner_id);
