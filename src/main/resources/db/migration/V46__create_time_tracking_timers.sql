CREATE TABLE IF NOT EXISTS time_tracking_timers (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT NULL,

    CONSTRAINT fk_time_tracking_timer_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_time_tracking_timer_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_time_tracking_timer_user_active
        UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_time_tracking_timer_user
    ON time_tracking_timers(user_id);

CREATE INDEX IF NOT EXISTS idx_time_tracking_timer_ticket
    ON time_tracking_timers(ticket_id);
