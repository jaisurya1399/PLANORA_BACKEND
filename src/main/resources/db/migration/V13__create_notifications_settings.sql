CREATE TABLE notifications (
    id UUID PRIMARY KEY,

    type VARCHAR(255) NOT NULL,

    notifiable_type VARCHAR(255) NOT NULL,

    notifiable_id BIGINT NOT NULL,

    data TEXT NOT NULL,

    read_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_notifications_notifiable
    ON notifications(notifiable_type, notifiable_id);

CREATE INDEX idx_notifications_read_at
    ON notifications(read_at);


CREATE TABLE settings (
    id BIGSERIAL PRIMARY KEY,

    "group" VARCHAR(255) NOT NULL,

    name VARCHAR(255) NOT NULL,

    locked BOOLEAN NOT NULL,

    payload TEXT NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL
);

CREATE INDEX idx_settings_group
    ON settings("group");