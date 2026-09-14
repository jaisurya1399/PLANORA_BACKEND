CREATE TABLE ticket_comments (
    id BIGSERIAL PRIMARY KEY,

    ticket_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    content TEXT NOT NULL,

    deleted_at TIMESTAMP NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_ticket_comments_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT fk_ticket_comments_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_ticket_comments_ticket_id
    ON ticket_comments(ticket_id);

CREATE INDEX idx_ticket_comments_user_id
    ON ticket_comments(user_id);

CREATE INDEX idx_ticket_comments_deleted_at
    ON ticket_comments(deleted_at);


CREATE TABLE ticket_activities (
    id BIGSERIAL PRIMARY KEY,

    ticket_id BIGINT NOT NULL,

    old_status_id BIGINT NOT NULL,

    new_status_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_ticket_activities_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT fk_ticket_activities_old_status
        FOREIGN KEY (old_status_id)
        REFERENCES ticket_statuses(id),

    CONSTRAINT fk_ticket_activities_new_status
        FOREIGN KEY (new_status_id)
        REFERENCES ticket_statuses(id),

    CONSTRAINT fk_ticket_activities_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_ticket_activities_ticket_id
    ON ticket_activities(ticket_id);

CREATE INDEX idx_ticket_activities_user_id
    ON ticket_activities(user_id);


CREATE TABLE ticket_relations (
    id BIGSERIAL PRIMARY KEY,

    ticket_id BIGINT NOT NULL,

    relation_id BIGINT NOT NULL,

    type VARCHAR(255) NOT NULL,

    sort INTEGER NOT NULL DEFAULT 1,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_ticket_relations_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT fk_ticket_relations_relation
        FOREIGN KEY (relation_id)
        REFERENCES tickets(id)
);

CREATE INDEX idx_ticket_relations_ticket_id
    ON ticket_relations(ticket_id);

CREATE INDEX idx_ticket_relations_relation_id
    ON ticket_relations(relation_id);


CREATE TABLE ticket_subscribers (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    ticket_id BIGINT NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_ticket_subscribers_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_ticket_subscribers_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT ticket_subscribers_unique
        UNIQUE (user_id, ticket_id)
);


CREATE INDEX idx_ticket_subscribers_user_id
    ON ticket_subscribers(user_id);

CREATE INDEX idx_ticket_subscribers_ticket_id
    ON ticket_subscribers(ticket_id);


CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    deleted_at TIMESTAMP NULL
);


CREATE INDEX idx_activities_deleted_at
    ON activities(deleted_at);


CREATE TABLE ticket_hours (
    id BIGSERIAL PRIMARY KEY,

    ticket_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    value NUMERIC(8,2),

    created_at TIMESTAMP NULL,

    updated_at TIMESTAMP NULL,

    comment TEXT NULL,

    activity_id BIGINT NULL,

    CONSTRAINT fk_ticket_hours_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT fk_ticket_hours_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_ticket_hours_activity
        FOREIGN KEY (activity_id)
        REFERENCES activities(id)
);

CREATE INDEX idx_ticket_hours_ticket_id
    ON ticket_hours(ticket_id);

CREATE INDEX idx_ticket_hours_user_id
    ON ticket_hours(user_id);

CREATE INDEX idx_ticket_hours_activity_id
    ON ticket_hours(activity_id);