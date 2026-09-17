-- V67: Disable the bundled development administrator credential.
-- Keeps the user row/data but requires an intentional password reset before use.
UPDATE users
SET password = '$2a$12$N8xjmoNUE/U6bBeWk43Mlu8MjN9aG8F3E11gSfBBQ0/UJWDAgSxry',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'jaisurya1399@gmail.com'
  AND password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
