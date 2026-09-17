-- ============================================================
-- V71: Planora project-aware RBAC
-- ============================================================
-- System role ADMIN remains the internal compatibility name for
-- the single System Admin. Project roles are first-class values:
-- PROJECT_ADMIN, TEAM_LEAD, DEVELOPER (VIEWER is retained as a
-- legacy read-only role).

-- ------------------------------------------------------------
-- Normalize existing project memberships.
-- ------------------------------------------------------------
ALTER TABLE project_users DROP CONSTRAINT IF EXISTS chk_project_users_role;
ALTER TABLE project_users DROP CONSTRAINT IF EXISTS chk_project_users_role_v49;

UPDATE project_users
SET role = CASE
    WHEN UPPER(TRIM(role)) IN ('ADMIN','OWNER','MANAGER','PROJECT_ADMIN') THEN 'PROJECT_ADMIN'
    WHEN UPPER(TRIM(role)) IN ('VIEWER','GUEST','READ_ONLY','READ-ONLY') THEN 'VIEWER'
    WHEN UPPER(TRIM(role)) = 'MEMBER' AND UPPER(TRIM(COALESCE(responsibility_role,''))) = 'TEAM_LEAD' THEN 'TEAM_LEAD'
    ELSE 'DEVELOPER'
END;

UPDATE project_users
SET responsibility_role = NULL
WHERE role <> 'MEMBER';

-- ------------------------------------------------------------
-- Every project must have at least one admin.
-- If historical data has no admin, promote the project owner.
-- If there are more than two, keep the oldest two and demote the
-- remainder to DEVELOPER so migration remains deterministic.
-- ------------------------------------------------------------
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY project_id ORDER BY id) AS rn
    FROM project_users
    WHERE role = 'PROJECT_ADMIN'
)
UPDATE project_users pu
SET role = 'DEVELOPER', responsibility_role = NULL
FROM ranked r
WHERE pu.id = r.id AND r.rn > 2;

INSERT INTO project_users (user_id, project_id, role, created_at, updated_at, responsibility_role, availability_self_update_open)
SELECT p.owner_id, p.id, 'PROJECT_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, FALSE
FROM projects p
WHERE p.owner_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM project_users pu
      WHERE pu.project_id = p.id AND pu.role = 'PROJECT_ADMIN'
  )
  AND NOT EXISTS (
      SELECT 1 FROM project_users pu
      WHERE pu.project_id = p.id AND pu.user_id = p.owner_id
  );

UPDATE project_users pu
SET role = 'PROJECT_ADMIN', responsibility_role = NULL
FROM projects p
WHERE pu.project_id = p.id
  AND p.owner_id = pu.user_id
  AND NOT EXISTS (
      SELECT 1 FROM project_users a
      WHERE a.project_id = p.id AND a.role = 'PROJECT_ADMIN'
  );

ALTER TABLE project_users
    ADD CONSTRAINT chk_project_users_role_v71
    CHECK (role IN ('PROJECT_ADMIN', 'TEAM_LEAD', 'DEVELOPER', 'VIEWER', 'MEMBER'));

-- ------------------------------------------------------------
-- Database-level cardinality protection.
-- Prevent >2 admins and prevent removing/demoting the final admin.
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION enforce_project_admin_cardinality()
RETURNS TRIGGER AS $$
DECLARE
    admin_count INTEGER;
BEGIN
    -- Serialize membership changes per project so concurrent requests cannot
    -- bypass the 2-admin limit. Cross-project updates lock both projects in
    -- deterministic order to avoid deadlocks.
    IF TG_OP = 'UPDATE' AND NEW.project_id <> OLD.project_id THEN
        PERFORM pg_advisory_xact_lock(LEAST(OLD.project_id, NEW.project_id));
        PERFORM pg_advisory_xact_lock(GREATEST(OLD.project_id, NEW.project_id));
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM pg_advisory_xact_lock(OLD.project_id);
    ELSE
        PERFORM pg_advisory_xact_lock(NEW.project_id);
    END IF;

    IF TG_OP = 'INSERT' AND NEW.role = 'PROJECT_ADMIN' THEN
        SELECT COUNT(*) INTO admin_count
        FROM project_users
        WHERE project_id = NEW.project_id
          AND role = 'PROJECT_ADMIN';
        IF admin_count >= 2 THEN
            RAISE EXCEPTION 'A project can have a maximum of 2 Project Admins';
        END IF;
    ELSIF TG_OP = 'UPDATE' THEN
        IF NEW.role = 'PROJECT_ADMIN'
           AND (OLD.role <> 'PROJECT_ADMIN' OR NEW.project_id <> OLD.project_id) THEN
            SELECT COUNT(*) INTO admin_count
            FROM project_users
            WHERE project_id = NEW.project_id
              AND role = 'PROJECT_ADMIN'
              AND id <> OLD.id;
            IF admin_count >= 2 THEN
                RAISE EXCEPTION 'A project can have a maximum of 2 Project Admins';
            END IF;
        END IF;
        IF OLD.role = 'PROJECT_ADMIN' AND NEW.project_id <> OLD.project_id THEN
            SELECT COUNT(*) INTO admin_count
            FROM project_users
            WHERE project_id = OLD.project_id
              AND role = 'PROJECT_ADMIN'
              AND id <> OLD.id;
            IF admin_count < 1 THEN
                RAISE EXCEPTION 'A project must always have at least 1 Project Admin';
            END IF;
        ELSIF OLD.role = 'PROJECT_ADMIN' AND NEW.role <> 'PROJECT_ADMIN' THEN
            SELECT COUNT(*) INTO admin_count
            FROM project_users
            WHERE project_id = OLD.project_id
              AND role = 'PROJECT_ADMIN'
              AND id <> OLD.id;
            IF admin_count < 1 THEN
                RAISE EXCEPTION 'A project must always have at least 1 Project Admin';
            END IF;
        END IF;
    ELSIF TG_OP = 'DELETE' AND OLD.role = 'PROJECT_ADMIN' THEN
        SELECT COUNT(*) INTO admin_count
        FROM project_users
        WHERE project_id = OLD.project_id
          AND role = 'PROJECT_ADMIN'
          AND id <> OLD.id;
        IF admin_count < 1 THEN
            RAISE EXCEPTION 'A project must always have at least 1 Project Admin';
        END IF;
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_project_admin_cardinality ON project_users;
CREATE TRIGGER trg_project_admin_cardinality
BEFORE INSERT OR UPDATE OR DELETE ON project_users
FOR EACH ROW EXECUTE FUNCTION enforce_project_admin_cardinality();

CREATE INDEX IF NOT EXISTS idx_project_users_project_role
    ON project_users(project_id, role);

-- ------------------------------------------------------------
-- Exactly one global System Admin (internal role name ADMIN).
-- ------------------------------------------------------------
DO $$
DECLARE admin_role_id BIGINT;
BEGIN
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN' AND guard_name = 'web' ORDER BY id LIMIT 1;
    IF admin_role_id IS NOT NULL THEN
        WITH ranked AS (
            SELECT model_id, model_type,
                   ROW_NUMBER() OVER (ORDER BY model_id, model_type) AS rn
            FROM model_has_roles
            WHERE role_id = admin_role_id
        )
        DELETE FROM model_has_roles m
        USING ranked r
        WHERE m.role_id = admin_role_id
          AND m.model_id = r.model_id
          AND m.model_type = r.model_type
          AND r.rn > 1;

        IF NOT EXISTS (SELECT 1 FROM model_has_roles WHERE role_id = admin_role_id) THEN
            INSERT INTO model_has_roles (role_id, model_id, model_type)
            SELECT admin_role_id, u.id, 'App\\Models\\User'
            FROM users u
            WHERE u.deleted_at IS NULL
            ORDER BY u.id
            LIMIT 1;
        END IF;
    END IF;
END $$;

CREATE OR REPLACE FUNCTION enforce_single_system_admin()
RETURNS TRIGGER AS $$
DECLARE
    is_admin BOOLEAN;
    admin_count INTEGER;
BEGIN
    PERFORM pg_advisory_xact_lock(2147483646);

    SELECT EXISTS (
        SELECT 1 FROM roles r
        WHERE r.id = CASE WHEN TG_OP = 'DELETE' THEN OLD.role_id ELSE NEW.role_id END
          AND r.name = 'ADMIN'
          AND r.guard_name = 'web'
    ) INTO is_admin;

    IF NOT is_admin THEN
        IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
        RETURN NEW;
    END IF;

    SELECT COUNT(*) INTO admin_count
    FROM model_has_roles m
    JOIN roles r ON r.id = m.role_id
    WHERE r.name = 'ADMIN'
      AND r.guard_name = 'web';

    IF TG_OP = 'INSERT' AND admin_count >= 1 THEN
        RAISE EXCEPTION 'Exactly one System Admin is allowed';
    ELSIF TG_OP = 'DELETE' AND admin_count <= 1 THEN
        RAISE EXCEPTION 'The System Admin role cannot be removed from the last System Admin';
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_single_system_admin ON model_has_roles;
CREATE TRIGGER trg_single_system_admin
BEFORE INSERT OR DELETE ON model_has_roles
FOR EACH ROW EXECUTE FUNCTION enforce_single_system_admin();
