-- Migration: Add complaint history table
-- Feature: Complaint Versioning & History Timeline
-- Description: Creates complaint_history table to track all changes to complaints

-- Create complaint_history table
CREATE TABLE IF NOT EXISTS complaint_history (
    id BIGSERIAL PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    field_changed VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    metadata TEXT,
    action_description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_complaint_history_complaint 
        FOREIGN KEY (complaint_id) REFERENCES complaints(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_complaint_history_actor 
        FOREIGN KEY (actor_id) REFERENCES users(id) 
        ON DELETE RESTRICT
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_complaint_history_complaint_id 
    ON complaint_history(complaint_id);
CREATE INDEX IF NOT EXISTS idx_complaint_history_created_at 
    ON complaint_history(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaint_history_action_type 
    ON complaint_history(action_type);

-- Add comments for documentation
COMMENT ON TABLE complaint_history IS 'Immutable timeline of all changes to complaints';
COMMENT ON COLUMN complaint_history.complaint_id IS 'Reference to the complaint';
COMMENT ON COLUMN complaint_history.actor_id IS 'User who performed the action';
COMMENT ON COLUMN complaint_history.action_type IS 'Type of action: CREATED, STATUS_CHANGED, UPDATED_FIELDS, ATTACHMENT_ADDED, ATTACHMENT_REMOVED, etc.';
COMMENT ON COLUMN complaint_history.field_changed IS 'Name of the field that was changed (nullable)';
COMMENT ON COLUMN complaint_history.old_value IS 'Previous value before change';
COMMENT ON COLUMN complaint_history.new_value IS 'New value after change';
COMMENT ON COLUMN complaint_history.metadata IS 'Additional structured data in JSON format';
COMMENT ON COLUMN complaint_history.action_description IS 'Human-readable description in Arabic';

-- Backfill existing complaints with CREATED entry
-- This creates a history entry for all existing complaints
INSERT INTO complaint_history (complaint_id, actor_id, action_type, action_description, created_at)
SELECT 
    c.id,
    c.citizen_id,
    'CREATED',
    'تم إنشاء الشكوى (استيراد قديم)',
    COALESCE(c.created_at, CURRENT_TIMESTAMP)
FROM complaints c
WHERE NOT EXISTS (
    SELECT 1 FROM complaint_history ch 
    WHERE ch.complaint_id = c.id AND ch.action_type = 'CREATED'
);

