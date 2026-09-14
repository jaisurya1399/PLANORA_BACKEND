-- ============================================================
-- V26 - Milestone Management
-- PostgreSQL
-- ============================================================


-- ============================================================
-- 1. CREATE MILESTONES TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS milestones (

    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,

    description TEXT,

    project_id BIGINT NOT NULL,

    start_date DATE,

    due_date DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    progress_percent INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT milestones_project_id_foreign
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_milestones_name_not_blank
        CHECK (
            length(trim(name)) > 0
        ),

    CONSTRAINT chk_milestones_status
        CHECK (
            status IN (
                'PLANNED',
                'IN_PROGRESS',
                'COMPLETED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_milestones_progress
        CHECK (
            progress_percent >= 0
            AND progress_percent <= 100
        ),

    CONSTRAINT chk_milestones_date_range
        CHECK (
            start_date IS NULL
            OR due_date IS NULL
            OR due_date >= start_date
        )
);


-- ============================================================
-- 2. INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_milestones_project_id
    ON milestones(project_id);

CREATE INDEX IF NOT EXISTS idx_milestones_status
    ON milestones(status);

CREATE INDEX IF NOT EXISTS idx_milestones_due_date
    ON milestones(due_date);

CREATE INDEX IF NOT EXISTS idx_milestones_project_status
    ON milestones(project_id, status);


-- ============================================================
-- 3. UNIQUE MILESTONE NAME PER PROJECT
-- ============================================================

CREATE UNIQUE INDEX IF NOT EXISTS
    uk_milestones_project_name
ON milestones (
    project_id,
    LOWER(TRIM(name))
);


-- ============================================================
-- 4. ADD MILESTONE TO TICKETS
-- ============================================================

ALTER TABLE tickets
ADD COLUMN IF NOT EXISTS milestone_id BIGINT;


-- ============================================================
-- 5. TICKET -> MILESTONE FOREIGN KEY
-- ============================================================

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname =
            'tickets_milestone_id_foreign'
    ) THEN

        ALTER TABLE tickets
        ADD CONSTRAINT tickets_milestone_id_foreign
        FOREIGN KEY (milestone_id)
        REFERENCES milestones(id)
        ON DELETE SET NULL;

    END IF;

END $$;


-- ============================================================
-- 6. TICKET MILESTONE INDEX
-- ============================================================

CREATE INDEX IF NOT EXISTS
    idx_tickets_milestone_id
ON tickets(milestone_id);


-- ============================================================
-- 7. PROJECT + MILESTONE TICKET INDEX
-- ============================================================

CREATE INDEX IF NOT EXISTS
    idx_tickets_project_milestone
ON tickets(
    project_id,
    milestone_id
);


-- ============================================================
-- 8. COMMENTS
-- ============================================================

COMMENT ON TABLE milestones IS
    'Project milestones used to track major delivery goals.';

COMMENT ON COLUMN milestones.name IS
    'Milestone name, unique within a project.';

COMMENT ON COLUMN milestones.description IS
    'Detailed milestone description.';

COMMENT ON COLUMN milestones.project_id IS
    'Project to which the milestone belongs.';

COMMENT ON COLUMN milestones.start_date IS
    'Milestone start date.';

COMMENT ON COLUMN milestones.due_date IS
    'Milestone target completion date.';

COMMENT ON COLUMN milestones.status IS
    'Milestone status: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED.';

COMMENT ON COLUMN milestones.progress_percent IS
    'Milestone completion percentage from 0 to 100.';

COMMENT ON COLUMN tickets.milestone_id IS
    'Milestone assigned to the ticket. NULL means no milestone.';


-- ============================================================
-- 9. VERIFY TABLE
-- ============================================================

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'milestones'
    ) THEN

        RAISE EXCEPTION
            'Milestone migration failed: milestones table was not created';

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'tickets'
        AND column_name = 'milestone_id'
    ) THEN

        RAISE EXCEPTION
            'Milestone migration failed: tickets.milestone_id was not created';

    END IF;

END $$;