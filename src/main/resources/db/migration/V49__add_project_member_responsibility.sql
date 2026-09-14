-- ============================================================
-- V49: Separate project access level from member responsibility
-- ============================================================
-- Project access levels:
--   PROJECT_ADMIN, MEMBER, VIEWER
-- Member responsibilities (only for MEMBER):
--   DEVELOPER, TESTER, TEAM_LEAD, SCRUM_MASTER,
--   PRODUCT_OWNER, BUSINESS_ANALYST
-- ============================================================

ALTER TABLE project_users
    ADD COLUMN responsibility_role VARCHAR(40);

ALTER TABLE project_users
    DROP CONSTRAINT IF EXISTS chk_project_users_role;

UPDATE project_users
SET role = 'PROJECT_ADMIN'
WHERE UPPER(TRIM(role)) IN ('ADMIN', 'PROJECT_ADMIN', 'OWNER', 'MANAGER');

UPDATE project_users
SET role = 'VIEWER'
WHERE UPPER(TRIM(role)) IN ('VIEWER', 'GUEST', 'READ_ONLY', 'READ-ONLY');

UPDATE project_users
SET role = 'MEMBER'
WHERE role IS NULL OR UPPER(TRIM(role)) NOT IN ('PROJECT_ADMIN', 'VIEWER', 'MEMBER');

UPDATE project_users
SET responsibility_role = 'DEVELOPER'
WHERE role = 'MEMBER'
  AND (responsibility_role IS NULL OR TRIM(responsibility_role) = '');

ALTER TABLE project_users
    ADD CONSTRAINT chk_project_users_role_v49
    CHECK (role IN ('PROJECT_ADMIN', 'MEMBER', 'VIEWER'));

ALTER TABLE project_users
    ADD CONSTRAINT chk_project_users_responsibility_v49
    CHECK (
        responsibility_role IS NULL
        OR responsibility_role IN (
            'DEVELOPER',
            'TESTER',
            'TEAM_LEAD',
            'SCRUM_MASTER',
            'PRODUCT_OWNER',
            'BUSINESS_ANALYST'
        )
    );
