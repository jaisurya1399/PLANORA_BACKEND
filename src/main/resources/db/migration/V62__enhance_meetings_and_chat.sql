ALTER TABLE chat_messages DROP CONSTRAINT IF EXISTS chat_direct_target;
ALTER TABLE chat_messages ADD CONSTRAINT chat_room_target CHECK (
 (room_type='DIRECT' AND recipient_id IS NOT NULL AND room_id IS NULL) OR
 (room_type IN ('PROJECT','MEETING') AND room_id IS NOT NULL AND recipient_id IS NULL)
);
ALTER TABLE project_meetings ADD COLUMN IF NOT EXISTS epic_id BIGINT NULL REFERENCES epics(id);
ALTER TABLE project_meetings ADD COLUMN IF NOT EXISTS meeting_type VARCHAR(20) NOT NULL DEFAULT 'ONLINE';
ALTER TABLE project_meetings ADD COLUMN IF NOT EXISTS location VARCHAR(1000);
ALTER TABLE project_meetings ADD COLUMN IF NOT EXISTS invite_all_team BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_project_meetings_epic ON project_meetings(epic_id);
CREATE TABLE IF NOT EXISTS meeting_attendees (
 id BIGSERIAL PRIMARY KEY,
 meeting_id BIGINT NOT NULL REFERENCES project_meetings(id) ON DELETE CASCADE,
 user_id BIGINT NOT NULL REFERENCES users(id),
 response_status VARCHAR(20) NOT NULL DEFAULT 'INVITED',
 CONSTRAINT uk_meeting_attendee UNIQUE(meeting_id,user_id)
);
CREATE INDEX IF NOT EXISTS idx_meeting_attendees_meeting ON meeting_attendees(meeting_id);
CREATE TABLE IF NOT EXISTS meeting_documents (
 id BIGSERIAL PRIMARY KEY,
 meeting_id BIGINT NOT NULL REFERENCES project_meetings(id) ON DELETE CASCADE,
 document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
 CONSTRAINT uk_meeting_document UNIQUE(meeting_id,document_id)
);
CREATE INDEX IF NOT EXISTS idx_meeting_documents_meeting ON meeting_documents(meeting_id);
