-- V73: Enforce ticket owner/responsible project membership at DB level.
-- A ticket cannot reference users who are not members of its project.

CREATE OR REPLACE FUNCTION enforce_ticket_project_membership()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.owner_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM project_users pu
        WHERE pu.project_id = NEW.project_id
          AND pu.user_id = NEW.owner_id
    ) THEN
        RAISE EXCEPTION 'Ticket owner must be a member of the selected project';
    END IF;

    IF NEW.responsible_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM project_users pu
        WHERE pu.project_id = NEW.project_id
          AND pu.user_id = NEW.responsible_id
    ) THEN
        RAISE EXCEPTION 'Ticket responsible user must be a member of the selected project';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_ticket_project_membership ON tickets;
CREATE TRIGGER trg_ticket_project_membership
BEFORE INSERT OR UPDATE OF project_id, owner_id, responsible_id ON tickets
FOR EACH ROW
EXECUTE FUNCTION enforce_ticket_project_membership();

CREATE INDEX IF NOT EXISTS idx_project_users_project_user
    ON project_users(project_id, user_id);
