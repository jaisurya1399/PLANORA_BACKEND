CREATE TABLE sprint_issue_snapshots (
    id BIGSERIAL PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    estimation NUMERIC(8,2) NOT NULL DEFAULT 0,
    resolved_at TIMESTAMP NULL,
    final_status_category VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sprint_issue_snapshots_sprint_id_foreign FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE CASCADE,
    CONSTRAINT uk_sprint_issue_snapshots_sprint_ticket UNIQUE (sprint_id, ticket_id)
);
CREATE INDEX idx_sprint_issue_snapshots_sprint_id ON sprint_issue_snapshots(sprint_id);
