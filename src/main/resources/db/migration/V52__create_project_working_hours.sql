CREATE TABLE IF NOT EXISTS project_working_hours (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    effective_from DATE NOT NULL,
    working_hours NUMERIC(5,2) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,

    CONSTRAINT project_working_hours_project_id_foreign
        FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT uk_project_working_hours_project_effective_from
        UNIQUE (project_id, effective_from),
    CONSTRAINT chk_project_working_hours_range
        CHECK (working_hours >= 0 AND working_hours <= 24)
);

CREATE INDEX IF NOT EXISTS idx_project_working_hours_project_effective
    ON project_working_hours(project_id, effective_from);
