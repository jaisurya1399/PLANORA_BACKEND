ALTER TABLE ticket_attachments ADD COLUMN IF NOT EXISTS comment_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_ticket_attachments_comment') THEN
        ALTER TABLE ticket_attachments ADD CONSTRAINT fk_ticket_attachments_comment
            FOREIGN KEY (comment_id) REFERENCES ticket_comments(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_ticket_attachments_comment_id ON ticket_attachments(comment_id);

CREATE TABLE IF NOT EXISTS ticket_comment_reactions (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_reactions_comment FOREIGN KEY (comment_id) REFERENCES ticket_comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_reactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_comment_reaction_user UNIQUE (comment_id, user_id, reaction)
);

CREATE INDEX IF NOT EXISTS idx_comment_reactions_comment ON ticket_comment_reactions(comment_id);
CREATE INDEX IF NOT EXISTS idx_comment_reactions_user ON ticket_comment_reactions(user_id);
