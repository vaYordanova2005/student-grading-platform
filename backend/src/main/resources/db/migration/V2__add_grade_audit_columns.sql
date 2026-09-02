ALTER TABLE grades ADD COLUMN teacher_id BIGINT REFERENCES users(id);
ALTER TABLE grades ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();

UPDATE grades SET teacher_id = (SELECT id FROM users WHERE role = 'TEACHER' ORDER BY id LIMIT 1) WHERE teacher_id IS NULL;

ALTER TABLE grades ALTER COLUMN teacher_id SET NOT NULL;
ALTER TABLE grades ALTER COLUMN created_at DROP DEFAULT;

CREATE INDEX idx_grades_teacher_id ON grades(teacher_id);
