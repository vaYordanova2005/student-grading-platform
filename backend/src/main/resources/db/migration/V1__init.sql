CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT'))
);

CREATE TABLE grades (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    subject VARCHAR(255) NOT NULL,
    semester INTEGER NOT NULL,
    grade INTEGER NOT NULL
);

CREATE INDEX idx_grades_student_id ON grades(student_id);
