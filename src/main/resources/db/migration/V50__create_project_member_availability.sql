CREATE TABLE IF NOT EXISTS project_member_availability (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    availability_date DATE NOT NULL,
    availability_type VARCHAR(20) NOT NULL,
    available_hours NUMERIC(5,2) NULL,
    reason VARCHAR(500) NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,

    CONSTRAINT project_member_availability_project_id_foreign
        FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT project_member_availability_user_id_foreign
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_project_member_availability_project_user_date
        UNIQUE (project_id, user_id, availability_date),
    CONSTRAINT chk_project_member_availability_hours
        CHECK (available_hours IS NULL OR (available_hours >= 0 AND available_hours <= 24)),
    CONSTRAINT chk_project_member_availability_type
        CHECK (availability_type IN ('AVAILABLE', 'HALF_DAY', 'HOLIDAY', 'UNAVAILABLE'))
);

CREATE INDEX IF NOT EXISTS idx_project_member_availability_project_date
    ON project_member_availability(project_id, availability_date);

CREATE INDEX IF NOT EXISTS idx_project_member_availability_user_date
    ON project_member_availability(user_id, availability_date);
