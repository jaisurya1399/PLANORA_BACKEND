INSERT INTO permissions (name, guard_name, created_at, updated_at) VALUES
('custom_field.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('custom_field.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('custom_field.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('custom_field.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('field_configuration.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('field_configuration.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('field_configuration.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('field_configuration.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('screen_configuration.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('screen_configuration.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('screen_configuration.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('screen_configuration.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT (name, guard_name) DO NOTHING;

INSERT INTO role_has_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN' AND p.name IN (
'custom_field.view','custom_field.create','custom_field.update','custom_field.delete',
'field_configuration.view','field_configuration.create','field_configuration.update','field_configuration.delete',
'screen_configuration.view','screen_configuration.create','screen_configuration.update','screen_configuration.delete'
)
ON CONFLICT DO NOTHING;
