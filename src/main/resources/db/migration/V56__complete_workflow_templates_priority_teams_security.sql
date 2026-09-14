-- Complete Jira-like configuration layer for the remaining P0/P1 gaps.
CREATE TABLE workflow_rules (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NULL REFERENCES projects(id) ON DELETE CASCADE,
 ticket_type_id BIGINT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
 from_status_id BIGINT NOT NULL REFERENCES ticket_statuses(id),
 to_status_id BIGINT NOT NULL REFERENCES ticket_statuses(id),
 required_permission VARCHAR(100) NOT NULL DEFAULT 'ticket.update',
 condition_json TEXT NULL,
 validator_json TEXT NULL,
 post_function_json TEXT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NULL,
 updated_at TIMESTAMP NULL
);
CREATE INDEX idx_workflow_rules_lookup ON workflow_rules(project_id,ticket_type_id,from_status_id,to_status_id,active);

CREATE TABLE ticket_templates (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id),
 status_id BIGINT NOT NULL REFERENCES ticket_statuses(id),
 priority_id BIGINT NOT NULL REFERENCES ticket_priorities(id),
 name VARCHAR(255) NOT NULL,
 content TEXT NULL,
 estimation NUMERIC(19,2) NULL,
 custom_fields_json TEXT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NULL,
 updated_at TIMESTAMP NULL
);
CREATE INDEX idx_ticket_templates_project ON ticket_templates(project_id,active);

ALTER TABLE ticket_priorities ADD COLUMN IF NOT EXISTS display_order INTEGER NOT NULL DEFAULT 0;
UPDATE ticket_priorities SET display_order=id WHERE display_order=0;

CREATE TABLE project_priority_schemes (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL UNIQUE REFERENCES projects(id) ON DELETE CASCADE,
 name VARCHAR(255) NOT NULL,
 priority_ids_json TEXT NOT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NULL,
 updated_at TIMESTAMP NULL
);

CREATE TABLE project_teams (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 name VARCHAR(255) NOT NULL,
 description TEXT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NULL,
 updated_at TIMESTAMP NULL,
 CONSTRAINT uq_project_team_name UNIQUE(project_id,name)
);
CREATE TABLE project_team_members (
 id BIGSERIAL PRIMARY KEY,
 team_id BIGINT NOT NULL REFERENCES project_teams(id) ON DELETE CASCADE,
 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 CONSTRAINT uq_team_member UNIQUE(team_id,user_id)
);

CREATE TABLE project_permission_schemes (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL UNIQUE REFERENCES projects(id) ON DELETE CASCADE,
 name VARCHAR(255) NOT NULL,
 grants_json TEXT NOT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NULL,
 updated_at TIMESTAMP NULL
);
CREATE TABLE issue_security_schemes (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL UNIQUE REFERENCES projects(id) ON DELETE CASCADE,
 name VARCHAR(255) NOT NULL,
 default_level VARCHAR(30) NOT NULL DEFAULT 'PROJECT',
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NULL,
 updated_at TIMESTAMP NULL,
 CONSTRAINT chk_issue_security_level CHECK(default_level IN ('PROJECT','MEMBERS','ASSIGNEE','REPORTER'))
);

INSERT INTO permissions(name,guard_name,created_at,updated_at) VALUES
('workflow.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('workflow.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('workflow.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('workflow.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('ticket_template.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('ticket_template.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('ticket_template.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('ticket_template.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('priority_scheme.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('priority_scheme.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('project_team.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('project_team.create','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('project_team.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('project_team.delete','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('permission_scheme.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('permission_scheme.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
('issue_security.view','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),('issue_security.update','web',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT(name,guard_name) DO NOTHING;
INSERT INTO role_has_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.name='ADMIN' AND p.name IN ('workflow.view','workflow.create','workflow.update','workflow.delete','ticket_template.view','ticket_template.create','ticket_template.update','ticket_template.delete','priority_scheme.view','priority_scheme.update','project_team.view','project_team.create','project_team.update','project_team.delete','permission_scheme.view','permission_scheme.update','issue_security.view','issue_security.update')
AND NOT EXISTS(SELECT 1 FROM role_has_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

CREATE TABLE workflow_schemes (
 id BIGSERIAL PRIMARY KEY,
 project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 name VARCHAR(255) NOT NULL,
 rule_ids_json TEXT NOT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP NULL,
 updated_at TIMESTAMP NULL
);
CREATE INDEX idx_workflow_schemes_project ON workflow_schemes(project_id,active);

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS security_level VARCHAR(30) NOT NULL DEFAULT 'PROJECT';
ALTER TABLE tickets ADD CONSTRAINT chk_ticket_security_level CHECK(security_level IN ('PROJECT','MEMBERS','ASSIGNEE','REPORTER'));
CREATE INDEX idx_tickets_security_level ON tickets(project_id,security_level);
