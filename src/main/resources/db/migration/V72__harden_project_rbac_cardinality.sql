-- ============================================================
-- V72: Harden Planora project/system RBAC cardinality
-- ============================================================
-- V71 introduced the project roles and cardinality triggers. This
-- migration closes the remaining UPDATE path for the global
-- System Admin role.

CREATE OR REPLACE FUNCTION enforce_single_system_admin()
RETURNS TRIGGER AS $$
DECLARE
    target_role_name TEXT;
    admin_count INTEGER;
BEGIN
    PERFORM pg_advisory_xact_lock(2147483646);

    SELECT r.name
      INTO target_role_name
      FROM roles r
     WHERE r.id = CASE WHEN TG_OP = 'DELETE' THEN OLD.role_id ELSE NEW.role_id END
       AND r.guard_name = 'web';

    IF target_role_name IS DISTINCT FROM 'ADMIN' THEN
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
    ELSIF TG_OP = 'UPDATE'
          AND OLD.role_id IS DISTINCT FROM NEW.role_id
          AND admin_count >= 1 THEN
        RAISE EXCEPTION 'Exactly one System Admin is allowed';
    ELSIF TG_OP = 'DELETE' AND admin_count <= 1 THEN
        RAISE EXCEPTION 'The System Admin role cannot be removed from the last System Admin';
    END IF;

    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_single_system_admin ON model_has_roles;
CREATE TRIGGER trg_single_system_admin
BEFORE INSERT OR UPDATE OF role_id OR DELETE ON model_has_roles
FOR EACH ROW EXECUTE FUNCTION enforce_single_system_admin();

-- Keep project membership role values deterministic.
UPDATE project_users
   SET role = 'DEVELOPER', responsibility_role = NULL
 WHERE role = 'MEMBER'
   AND UPPER(TRIM(COALESCE(responsibility_role, ''))) NOT IN ('TEAM_LEAD', 'DEVELOPER');

UPDATE project_users
   SET role = 'TEAM_LEAD', responsibility_role = NULL
 WHERE role = 'MEMBER'
   AND UPPER(TRIM(COALESCE(responsibility_role, ''))) = 'TEAM_LEAD';

UPDATE project_users
   SET role = 'DEVELOPER', responsibility_role = NULL
 WHERE role = 'MEMBER'
   AND UPPER(TRIM(COALESCE(responsibility_role, ''))) = 'DEVELOPER';
