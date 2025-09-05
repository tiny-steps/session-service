-- Add branch context to session_offerings table
-- This migration adds branch_id column to support multi-branch functionality

ALTER TABLE session_offerings 
ADD COLUMN branch_id UUID;

-- Add index for better query performance on branch_id
CREATE INDEX idx_session_offerings_branch_id ON session_offerings(branch_id);

-- Add composite index for common query patterns
CREATE INDEX idx_session_offerings_doctor_branch ON session_offerings(doctor_id, branch_id);
CREATE INDEX idx_session_offerings_session_type_branch ON session_offerings(session_type_id, branch_id);

-- Add comment for documentation
COMMENT ON COLUMN session_offerings.branch_id IS 'Branch ID for multi-branch support - links session offering to specific branch';