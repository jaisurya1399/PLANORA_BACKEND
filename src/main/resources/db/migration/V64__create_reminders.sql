CREATE TABLE IF NOT EXISTS reminders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    remind_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    triggered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    CONSTRAINT chk_reminders_status CHECK (status IN ('PENDING','TRIGGERED','CANCELLED'))
);
CREATE INDEX IF NOT EXISTS idx_reminders_user_due ON reminders(user_id, remind_at, status);

INSERT INTO permissions (name, guard_name, created_at, updated_at)
VALUES
 ('reminder.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
 ('reminder.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
 ('reminder.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT (name, guard_name) DO NOTHING;
