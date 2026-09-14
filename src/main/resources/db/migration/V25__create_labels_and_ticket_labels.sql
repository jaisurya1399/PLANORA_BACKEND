-- ============================================================
-- V25 - Labels Management
-- PostgreSQL
-- ============================================================


-- ============================================================
-- 1. CREATE LABELS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS labels (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    color VARCHAR(20) NOT NULL DEFAULT '#1976D2',

    description TEXT,

    project_id BIGINT NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT labels_project_id_foreign
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_labels_name_not_blank
        CHECK (length(trim(name)) > 0),

    CONSTRAINT chk_labels_color
        CHECK (
            color ~ '^#[0-9A-Fa-f]{6}$'
        )
);


-- ============================================================
-- 2. LABEL INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_labels_project_id
    ON labels(project_id);

CREATE INDEX IF NOT EXISTS idx_labels_name
    ON labels(name);


-- ============================================================
-- 3. UNIQUE LABEL NAME PER PROJECT
-- ============================================================

CREATE UNIQUE INDEX IF NOT EXISTS uk_labels_project_name
    ON labels(
        project_id,
        LOWER(TRIM(name))
    );


-- ============================================================
-- 4. CREATE TICKET-LABEL MAPPING TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS ticket_labels (
    ticket_id BIGINT NOT NULL,

    label_id BIGINT NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ticket_labels
        PRIMARY KEY (
            ticket_id,
            label_id
        ),

    CONSTRAINT ticket_labels_ticket_id_foreign
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE CASCADE,

    CONSTRAINT ticket_labels_label_id_foreign
        FOREIGN KEY (label_id)
        REFERENCES labels(id)
        ON DELETE CASCADE
);


-- ============================================================
-- 5. TICKET-LABEL INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_ticket_labels_ticket_id
    ON ticket_labels(ticket_id);

CREATE INDEX IF NOT EXISTS idx_ticket_labels_label_id
    ON ticket_labels(label_id);


-- ============================================================
-- 6. COMMENTS
-- ============================================================

COMMENT ON TABLE labels IS
    'Project-specific labels used to categorize tickets.';

COMMENT ON COLUMN labels.name IS
    'Unique label name within a project.';

COMMENT ON COLUMN labels.color IS
    'HEX color used to display the label.';

COMMENT ON COLUMN labels.description IS
    'Optional label description.';

COMMENT ON COLUMN labels.project_id IS
    'Project to which the label belongs.';


COMMENT ON TABLE ticket_labels IS
    'Many-to-many relationship between tickets and labels.';


-- ============================================================
-- 7. VERIFY MIGRATION
-- ============================================================

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'labels'
    ) THEN

        RAISE EXCEPTION
            'Labels migration failed: labels table was not created';

    END IF;


    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'ticket_labels'
    ) THEN

        RAISE EXCEPTION
            'Labels migration failed: ticket_labels table was not created';

    END IF;

END $$;