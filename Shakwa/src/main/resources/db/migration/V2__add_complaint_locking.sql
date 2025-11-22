-- Migration: Add optimistic locking version column
-- Feature: Concurrency Control / Complaint Locking
-- Description: Adds version column for optimistic locking. State-based locking uses existing status and responded_by columns.

-- Add version column for optimistic locking
ALTER TABLE complaints
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Update existing records to have version 0
UPDATE complaints SET version = 0 WHERE version IS NULL;

-- Add comment for documentation
COMMENT ON COLUMN complaints.version IS 'Version number for optimistic locking - automatically incremented on each update';
COMMENT ON COLUMN complaints.status IS 'Complaint status - IN_PROGRESS status acts as a virtual lock when responded_by is set';
COMMENT ON COLUMN complaints.responded_by IS 'Employee who is processing the complaint - used for state-based locking';

