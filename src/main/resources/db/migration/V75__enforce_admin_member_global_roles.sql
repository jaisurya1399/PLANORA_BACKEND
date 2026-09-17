-- V76: Global role model is ADMIN / MEMBER only.
-- ADMIN is the sole persisted global role; all other users are MEMBER.

-- Ensure MEMBER exists for UI/audit semantics if callers need a role record.
INSERT INTO roles (name, guard_name, created_at, updated_at)
SELECT 'MEMBER', 'web', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'MEMBER' AND guard_name = 'web'
);

-- Remove every non-ADMIN global role assignment. Project roles remain in
-- project_users and are intentionally not represented in model_has_roles.
DELETE FROM model_has_roles m
USING roles r
WHERE m.role_id = r.id
  AND r.guard_name = 'web'
  AND r.name <> 'ADMIN';

-- A global MEMBER role is not granted application permissions.
DELETE FROM role_has_permissions rp
USING roles r
WHERE rp.role_id = r.id
  AND r.name = 'MEMBER'
  AND r.guard_name = 'web';
