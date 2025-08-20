-- V2 Migration: Allow doctor_id and practice_id to be nullable in doctor_session_offerings table

-- Drop the existing unique constraint that includes doctor_id and practice_id
ALTER TABLE doctor_session_offerings DROP CONSTRAINT if exists doctor_session_offerings_doctor_id_practice_id_session_type_id_key;

-- Modify doctor_id to allow NULL values
ALTER TABLE doctor_session_offerings ALTER COLUMN doctor_id DROP NOT NULL;

-- Modify practice_id to allow NULL values
ALTER TABLE doctor_session_offerings ALTER COLUMN practice_id DROP NOT NULL;

-- Create a new unique constraint that handles nullable values properly
-- This constraint will ensure uniqueness while allowing NULL values
CREATE UNIQUE INDEX idx_dso_unique_offering ON doctor_session_offerings(
    COALESCE(doctor_id, '00000000-0000-0000-0000-000000000000'::uuid),
    COALESCE(practice_id, '00000000-0000-0000-0000-000000000000'::uuid),
    session_type_id
);

-- Add a check constraint to ensure at least one of doctor_id or practice_id is not null
ALTER TABLE doctor_session_offerings ADD CONSTRAINT chk_doctor_or_practice_required
    CHECK (doctor_id IS NOT NULL OR practice_id IS NOT NULL);
