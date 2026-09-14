CREATE TABLE daily_scrums (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    scrum_date DATE NOT NULL,

    yesterday_work TEXT NOT NULL,
    today_work TEXT NOT NULL,
    blockers TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_daily_scrums_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_daily_scrums_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT uk_daily_scrum_user_project_date
        UNIQUE (user_id, project_id, scrum_date)
);

CREATE INDEX idx_daily_scrums_user_id
    ON daily_scrums(user_id);

CREATE INDEX idx_daily_scrums_project_id
    ON daily_scrums(project_id);

CREATE INDEX idx_daily_scrums_scrum_date
    ON daily_scrums(scrum_date);

CREATE INDEX idx_daily_scrums_user_date
    ON daily_scrums(user_id, scrum_date);

CREATE INDEX idx_daily_scrums_project_date
    ON daily_scrums(project_id, scrum_date);