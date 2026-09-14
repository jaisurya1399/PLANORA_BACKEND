UPDATE project_users
SET role = CASE
    WHEN UPPER(TRIM(role)) IN ('OWNER', 'ADMIN', 'PROJECT_ADMIN', 'MANAGER') THEN 'ADMIN'
    WHEN UPPER(TRIM(role)) IN ('VIEWER', 'GUEST', 'READ_ONLY', 'READ-ONLY') THEN 'VIEWER'
    ELSE 'MEMBER'
END;

ALTER TABLE project_users
    ADD CONSTRAINT chk_project_users_role CHECK (role IN ('ADMIN', 'MEMBER', 'VIEWER'));
