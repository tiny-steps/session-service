-- V3 Migration: Remove practice_id column from doctor_session_offerings table
-- This migration removes all Practice entity dependencies from the session service

-- Drop the unique index that includes practice_id
DROP INDEX IF EXISTS idx_dso_unique_offering;

-- Drop the practice_id index
DROP INDEX IF EXISTS idx_dso_practice_id;

-- Drop the check constraint that required doctor_id or practice_id
ALTER TABLE doctor_session_offerings DROP CONSTRAINT IF EXISTS chk_doctor_or_practice_required;

-- Remove the practice_id column entirely
ALTER TABLE doctor_session_offerings DROP COLUMN IF EXISTS practice_id;

-- Make doctor_id NOT NULL again since practice_id is removed
ALTER TABLE doctor_session_offerings ALTER COLUMN doctor_id SET NOT NULL;

-- Create a new unique constraint for doctor_id and session_type_id only
CREATE UNIQUE INDEX idx_dso_doctor_session_unique ON doctor_session_offerings(doctor_id, session_type_id);

-- Add comment to document the change
COMMENT ON TABLE doctor_session_offerings IS 'Doctor session offerings - practice_id removed as part of Practice entity elimination';