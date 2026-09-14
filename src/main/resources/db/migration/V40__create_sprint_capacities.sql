CREATE TABLE sprint_capacities (
    id BIGSERIAL PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    capacity_points NUMERIC(10,2) NOT NULL DEFAULT 0,
    capacity_hours NUMERIC(10,2) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NULL,
    CONSTRAINT sprint_capacities_sprint_id_foreign FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE CASCADE,
    CONSTRAINT sprint_capacities_user_id_foreign FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_sprint_capacities_sprint_user UNIQUE (sprint_id, user_id)
);
CREATE INDEX idx_sprint_capacities_sprint_id ON sprint_capacities(sprint_id);
