CREATE TABLE IF NOT EXISTS automation_rules (
 id BIGSERIAL PRIMARY KEY, project_id BIGINT NULL REFERENCES projects(id), name VARCHAR(150) NOT NULL,
 trigger_event VARCHAR(80) NOT NULL, condition_expression VARCHAR(80) NOT NULL DEFAULT 'ALWAYS',
 action_type VARCHAR(80) NOT NULL, action_value TEXT, enabled BOOLEAN NOT NULL DEFAULT TRUE,
 last_run_at TIMESTAMP NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE INDEX IF NOT EXISTS idx_automation_rules_event ON automation_rules(trigger_event, enabled);

CREATE TABLE IF NOT EXISTS notification_schemes (id BIGSERIAL PRIMARY KEY, project_id BIGINT NOT NULL REFERENCES projects(id), name VARCHAR(150) NOT NULL, enabled BOOLEAN NOT NULL DEFAULT TRUE, UNIQUE(project_id));
CREATE TABLE IF NOT EXISTS notification_scheme_rules (id BIGSERIAL PRIMARY KEY, scheme_id BIGINT NOT NULL REFERENCES notification_schemes(id) ON DELETE CASCADE, event_type VARCHAR(100) NOT NULL, recipient_type VARCHAR(50) NOT NULL, in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE, email_enabled BOOLEAN NOT NULL DEFAULT TRUE);

CREATE TABLE IF NOT EXISTS sla_policies (id BIGSERIAL PRIMARY KEY, project_id BIGINT NOT NULL REFERENCES projects(id), name VARCHAR(150) NOT NULL, target_hours INTEGER NOT NULL, priority_filter VARCHAR(50), enabled BOOLEAN NOT NULL DEFAULT TRUE);

CREATE TABLE IF NOT EXISTS wiki_pages (id BIGSERIAL PRIMARY KEY, project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE, parent_id BIGINT NULL REFERENCES wiki_pages(id) ON DELETE SET NULL, title VARCHAR(255) NOT NULL, content TEXT, updated_by BIGINT NULL REFERENCES users(id), version INTEGER NOT NULL DEFAULT 1, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE INDEX IF NOT EXISTS idx_wiki_pages_project ON wiki_pages(project_id);

CREATE TABLE IF NOT EXISTS portfolios (id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, description TEXT, owner_id BIGINT NOT NULL REFERENCES users(id), active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS portfolio_projects (id BIGSERIAL PRIMARY KEY, portfolio_id BIGINT NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE, project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE, target_percent INTEGER, UNIQUE(portfolio_id, project_id));

CREATE TABLE IF NOT EXISTS project_risks (id BIGSERIAL PRIMARY KEY, project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE, title VARCHAR(255) NOT NULL, description TEXT, probability VARCHAR(30) NOT NULL, impact VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL, owner VARCHAR(255), mitigation TEXT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE INDEX IF NOT EXISTS idx_project_risks_project ON project_risks(project_id);

CREATE TABLE IF NOT EXISTS ticket_dependencies (id BIGSERIAL PRIMARY KEY, source_ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE, target_ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE, type VARCHAR(30) NOT NULL, description TEXT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, UNIQUE(source_ticket_id,target_ticket_id));
CREATE INDEX IF NOT EXISTS idx_ticket_dependencies_source ON ticket_dependencies(source_ticket_id);
CREATE INDEX IF NOT EXISTS idx_ticket_dependencies_target ON ticket_dependencies(target_ticket_id);
