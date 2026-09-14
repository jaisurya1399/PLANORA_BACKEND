CREATE TABLE chat_messages (
 id BIGSERIAL PRIMARY KEY,
 room_type VARCHAR(20) NOT NULL,
 room_id BIGINT NULL,
 sender_id BIGINT NOT NULL REFERENCES users(id),
 recipient_id BIGINT NULL REFERENCES users(id),
 content TEXT NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chat_direct_target CHECK ((room_type='DIRECT' AND recipient_id IS NOT NULL AND room_id IS NULL) OR (room_type='PROJECT' AND room_id IS NOT NULL AND recipient_id IS NULL))
);
CREATE INDEX idx_chat_direct ON chat_messages(sender_id, recipient_id, created_at);
CREATE INDEX idx_chat_room ON chat_messages(room_type, room_id, created_at);
