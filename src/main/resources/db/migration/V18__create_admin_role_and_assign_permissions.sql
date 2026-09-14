-- ============================================================
-- V18: Create ADMIN role and assign all permissions
-- ============================================================

-- ------------------------------------------------------------
-- Create ADMIN role if it does not already exist
-- ------------------------------------------------------------

INSERT INTO roles (
    name,
    guard_name,
    created_at,
    updated_at
)
SELECT
    'ADMIN',
    'web',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE name = 'ADMIN'
      AND guard_name = 'web'
);


-- ------------------------------------------------------------
-- Assign every permission to ADMIN
--
-- role_has_permissions has composite primary key:
-- (permission_id, role_id)
-- ------------------------------------------------------------

INSERT INTO role_has_permissions (
    role_id,
    permission_id
)
SELECT
    r.id,
    p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND r.guard_name = 'web'
  AND NOT EXISTS (
      SELECT 1
      FROM role_has_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );