CREATE TABLE IF NOT EXISTS releases (
    id BIGSERIAL PRIMARY KEY,
    version VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_notes TEXT,
    project_id BIGINT NOT NULL,
    start_date DATE,
    release_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT releases_project_id_foreign FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT chk_releases_status CHECK (status IN ('PLANNED','IN_PROGRESS','RELEASED','CANCELLED')),
    CONSTRAINT chk_releases_date_range CHECK (start_date IS NULL OR release_date IS NULL OR release_date >= start_date)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_releases_project_version ON releases(project_id, LOWER(TRIM(version)));
CREATE INDEX IF NOT EXISTS idx_releases_project_id ON releases(project_id);
CREATE INDEX IF NOT EXISTS idx_releases_release_date ON releases(release_date);
CREATE INDEX IF NOT EXISTS idx_releases_status ON releases(status);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS release_id BIGINT;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='tickets_release_id_foreign') THEN ALTER TABLE tickets ADD CONSTRAINT tickets_release_id_foreign FOREIGN KEY (release_id) REFERENCES releases(id) ON DELETE SET NULL; END IF; END $$;
CREATE INDEX IF NOT EXISTS idx_tickets_release_id ON tickets(release_id);
INSERT INTO permissions(name,guard_name,created_at,updated_at) VALUES ('release.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('release.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('release.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('release.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT (name,guard_name) DO NOTHING;
