-- ============================================================
-- V20: Create default ADMIN user
-- ============================================================

INSERT INTO users (
    name,
    email,
    password,
    created_at,
    updated_at
)
SELECT
    'Jai Surya',
    'jaisurya1399@gmail.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'jaisurya1399@gmail.com'
);

-- ============================================================
-- Assign ADMIN role to default user
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