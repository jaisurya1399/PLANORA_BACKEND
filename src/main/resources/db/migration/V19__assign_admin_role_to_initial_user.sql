-- ============================================================
-- V19: Create initial ADMIN role assignment
-- ============================================================
--
-- IMPORTANT:
-- This migration does NOT create a user.
--
-- The application should create the first user through the
-- registration/user creation flow and then assign ADMIN.
-- ============================================================

INSERT INTO model_has_roles (
    role_id,
    model_id,
    model_type
)
SELECT
    r.id,
    u.id,
    'com.projectmanagement.app.user.User'
FROM roles r
CROSS JOIN users u
WHERE r.name = 'ADMIN'
  AND r.guard_name = 'web'
  AND u.email = 'jaisurya1399@gmail.com'
  AND NOT EXISTS (
      SELECT 1
      FROM model_has_roles mhr
      WHERE mhr.role_id = r.id
        AND mhr.model_id = u.id
        AND mhr.model_type = 'com.projectmanagement.app.user.User'
  );