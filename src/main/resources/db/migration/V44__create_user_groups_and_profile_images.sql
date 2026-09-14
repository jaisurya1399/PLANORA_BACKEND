ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_data BYTEA NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_content_type VARCHAR(100) NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_file_name VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS user_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_groups_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS user_group_members (
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_user_group_members PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_user_group_members_group FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_group_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_group_members_user_id ON user_group_members(user_id);
