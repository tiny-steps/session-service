-- V4 Migration: Rename doctor_session_offerings table to session_offerings
-- This migration renames the table to better reflect the domain model

-- Rename the table
ALTER TABLE doctor_session_offerings RENAME TO session_offerings;

-- Update table comment
COMMENT ON TABLE session_offerings IS 'Session offerings - renamed from doctor_session_offerings for better domain alignment';

-- Note: All existing indexes and constraints are automatically renamed by PostgreSQL
-- when the table is renamed, so no additional changes are needed