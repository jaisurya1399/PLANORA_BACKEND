-- ============================================================
-- V24 - Create Sprint Management
-- ============================================================
-- Creates:
--   1. sprints table
--   2. Sprint indexes
--   3. Sprint -> Project foreign key
--   4. Sprint -> User(created_by) foreign key
--   5. Ticket -> Sprint relationship
--   6. Ticket sprint index
--   7. Validation constraints
--
-- PostgreSQL
-- ============================================================


-- ============================================================
-- 1. CREATE SPRINTS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS sprints (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    goal TEXT,

    project_id BIGINT NOT NULL,

    start_date DATE,

    end_date DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    created_by BIGINT,

    created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT sprints_project_id_foreign
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT sprints_created_by_foreign
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_sprints_status
        CHECK (
            status IN (
                'PLANNED',
                'ACTIVE',
                'COMPLETED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_sprints_date_range
        CHECK (
            start_date IS NULL
            OR end_date IS NULL
            OR end_date >= start_date
        )
);


-- ============================================================
-- 2. SPRINT INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_sprints_project_id
    ON sprints(project_id);

CREATE INDEX IF NOT EXISTS idx_sprints_status
    ON sprints(status);

CREATE INDEX IF NOT EXISTS idx_sprints_project_status
    ON sprints(project_id, status);

CREATE INDEX IF NOT EXISTS idx_sprints_start_date
    ON sprints(start_date);

CREATE INDEX IF NOT EXISTS idx_sprints_end_date
    ON sprints(end_date);


-- ============================================================
-- 3. ADD SPRINT TO TICKETS
-- ============================================================
-- A ticket can belong to zero or one sprint.
-- NULL means the ticket is currently in project backlog.

ALTER TABLE tickets
ADD COLUMN IF NOT EXISTS sprint_id BIGINT;


-- ============================================================
-- 4. TICKET -> SPRINT FOREIGN KEY
-- ============================================================
-- When a sprint is deleted, tickets are moved back to backlog
-- instead of being deleted.

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'tickets_sprint_id_foreign'
    ) THEN

        ALTER TABLE tickets
        ADD CONSTRAINT tickets_sprint_id_foreign
        FOREIGN KEY (sprint_id)
        REFERENCES sprints(id)
        ON DELETE SET NULL;

    END IF;

END $$;


-- ============================================================
-- 5. TICKET SPRINT INDEX
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_tickets_sprint_id
    ON tickets(sprint_id);


-- ============================================================
-- 6. COMPOSITE INDEX
-- ============================================================
-- Useful for queries like:
-- Get all tickets of a project inside a sprint.

CREATE INDEX IF NOT EXISTS idx_tickets_project_sprint
    ON tickets(project_id, sprint_id);


-- ============================================================
-- 7. OPTIONAL UNIQUE SPRINT NAME PER PROJECT
-- ============================================================
-- Same sprint name can exist in different projects.
-- But duplicate sprint names inside the same project are avoided.

CREATE UNIQUE INDEX IF NOT EXISTS uk_sprints_project_name
    ON sprints(project_id, LOWER(name));


-- ============================================================
-- 8. COMMENTS
-- ============================================================

COMMENT ON TABLE sprints IS
    'Stores project sprints and their lifecycle information.';

COMMENT ON COLUMN sprints.name IS
    'Sprint name.';

COMMENT ON COLUMN sprints.goal IS
    'Sprint goal or objective.';

COMMENT ON COLUMN sprints.project_id IS
    'Project to which the sprint belongs.';

COMMENT ON COLUMN sprints.start_date IS
    'Sprint start date.';

COMMENT ON COLUMN sprints.end_date IS
    'Sprint end date.';

COMMENT ON COLUMN sprints.status IS
    'Sprint lifecycle status: PLANNED, ACTIVE, COMPLETED, CANCELLED.';

COMMENT ON COLUMN sprints.created_by IS
    'User who created the sprint.';

COMMENT ON COLUMN tickets.sprint_id IS
    'Sprint assigned to the ticket. NULL means the ticket is in backlog.';


-- ============================================================
-- 9. VERIFY MIGRATION
-- ============================================================

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'sprints'
    ) THEN

        RAISE EXCEPTION
            'Sprint migration failed: sprints table was not created';

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'tickets'
        AND column_name = 'sprint_id'
    ) THEN

        RAISE EXCEPTION
            'Sprint migration failed: tickets.sprint_id was not created';

    END IF;

END $$;