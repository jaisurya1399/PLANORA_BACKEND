-- ============================================================
-- V21: Complete activities master table
-- ============================================================

-- Existing activities table is already present.
-- Add the missing columns/constraints required by the
-- Activity module.

ALTER TABLE activities
ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT FALSE;

-- Make description nullable because the ActivityRequest
-- allows description to be optional.
ALTER TABLE activities
ALTER COLUMN description DROP NOT NULL;

-- Create case-insensitive unique index for active activities.
-- Soft-deleted activities can have the same name again.
CREATE UNIQUE INDEX IF NOT EXISTS uq_activities_name_active
ON activities (LOWER(name))
WHERE deleted_at IS NULL;

-- Index for soft-delete filtering.
CREATE INDEX IF NOT EXISTS idx_activities_deleted_at
ON activities (deleted_at);

-- Index for default active activities.
CREATE INDEX IF NOT EXISTS idx_activities_is_default
ON activities (is_default)
WHERE deleted_at IS NULL;

-- Seed default activities if they don't already exist.
INSERT INTO activities (name, description, is_default)
SELECT
    'Programming',
    'Software development and programming activities',
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE LOWER(name) = LOWER('Programming')
);

INSERT INTO activities (name, description, is_default)
SELECT
    'Testing',
    'Software testing and quality assurance activities',
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE LOWER(name) = LOWER('Testing')
);

INSERT INTO activities (name, description, is_default)
SELECT
    'Learning',
    'Learning, training and skill development activities',
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE LOWER(name) = LOWER('Learning')
);

INSERT INTO activities (name, description, is_default)
SELECT
    'Research',
    'Research, analysis and investigation activities',
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE LOWER(name) = LOWER('Research')
);

INSERT INTO activities (name, description, is_default)
SELECT
    'Others',
    'Other activities that do not belong to the defined categories',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE LOWER(name) = LOWER('Others')
);