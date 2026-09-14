CREATE TABLE project_meetings (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL REFERENCES projects(id),
 title VARCHAR(255) NOT NULL,
 agenda TEXT,
 starts_at TIMESTAMP NOT NULL,
 ends_at TIMESTAMP,
 meeting_url VARCHAR(1000),
 status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
 created_by BIGINT NOT NULL REFERENCES users(id),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_project_meetings_project_time ON project_meetings(project_id, starts_at);
