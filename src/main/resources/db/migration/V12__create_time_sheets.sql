CREATE TABLE time_sheets (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    project_id BIGINT NULL,

    task TEXT NULL,

    value NUMERIC(8,2),

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    deleted_at TIMESTAMP NULL,

    CONSTRAINT fk_time_sheets_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_time_sheets_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
);

CREATE INDEX idx_time_sheets_user_id
    ON time_sheets(user_id);

CREATE INDEX idx_time_sheets_project_id
    ON time_sheets(project_id);

CREATE INDEX idx_time_sheets_deleted_at
    ON time_sheets(deleted_at);


CREATE TABLE time_sheet_cells (
    id BIGSERIAL PRIMARY KEY,

    time_sheet_id BIGINT NOT NULL,

    value NUMERIC(8,2),

    is_trip BOOLEAN NOT NULL DEFAULT FALSE,

    comment TEXT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    date DATE NOT NULL,

    CONSTRAINT fk_time_sheet_cells_time_sheet
        FOREIGN KEY (time_sheet_id)
        REFERENCES time_sheets(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_time_sheet_cells_time_sheet_id
    ON time_sheet_cells(time_sheet_id);

CREATE INDEX idx_time_sheet_cells_date
    ON time_sheet_cells(date);