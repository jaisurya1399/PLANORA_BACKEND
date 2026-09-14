-- V68: Remove the bundled administrator password from production databases.
-- The historical seed migration created a well-known development credential.
UPDATE users
SET password = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin@gmail.com';
