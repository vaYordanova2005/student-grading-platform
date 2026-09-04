CREATE TABLE student_profiles (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    degree_level VARCHAR(100),
    faculty_number VARCHAR(50),
    faculty VARCHAR(255),
    specialty VARCHAR(255),
    study_mode VARCHAR(100),
    specialization VARCHAR(255),
    group_number VARCHAR(50),
    admission_type VARCHAR(100),
    status VARCHAR(100),
    enrolled_semester INTEGER,
    completed_semester INTEGER,
    stream VARCHAR(50),
    personal_email VARCHAR(255)
);
