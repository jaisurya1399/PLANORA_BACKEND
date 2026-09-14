-- ============================================================
-- V27 - Link Epics to Milestones
-- ============================================================

ALTER TABLE epics
ADD COLUMN IF NOT EXISTS milestone_id BIGINT;

-- Existing epics may predate milestone management, so keep this
-- column nullable for backward compatibility. New Epic API requests
-- require a milestone.
CREATE INDEX IF NOT EXISTS idx_epics_milestone_id
ON epics(milestone_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'epics_milestone_id_foreign'
    ) THEN
        ALTER TABLE epics
        ADD CONSTRAINT epics_milestone_id_foreign
        FOREIGN KEY (milestone_id)
        REFERENCES milestones(id)
        ON DELETE SET NULL;
    END IF;
END $$;

COMMENT ON COLUMN epics.milestone_id IS
    'Milestone under which this Epic is planned.';

