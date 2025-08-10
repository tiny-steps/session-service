CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE session_types (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               name VARCHAR(100) NOT NULL,
                               description TEXT,
                               default_duration_minutes INT NOT NULL CHECK (default_duration_minutes > 0),
                               is_telemedicine_available BOOLEAN DEFAULT false,
                               is_active BOOLEAN DEFAULT true,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_session_types_name ON session_types(LOWER(name));

CREATE TABLE doctor_session_offerings (
                                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                          doctor_id UUID NOT NULL,
                                          practice_id UUID NOT NULL,
                                          session_type_id UUID NOT NULL REFERENCES session_types(id) ON DELETE CASCADE,
                                          price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
                                          is_active BOOLEAN DEFAULT true,
                                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                          UNIQUE (doctor_id, practice_id, session_type_id)
);

CREATE INDEX idx_dso_doctor_id ON doctor_session_offerings(doctor_id);
CREATE INDEX idx_dso_practice_id ON doctor_session_offerings(practice_id);
CREATE INDEX idx_dso_session_type_id ON doctor_session_offerings(session_type_id);
