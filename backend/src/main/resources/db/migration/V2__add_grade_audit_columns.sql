ALTER TABLE grades ADD COLUMN teacher_id BIGINT REFERENCES users(id);
ALTER TABLE grades ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();

-- Best-effort attribution for any pre-existing rows: there's no real record
-- of who entered them, so we attribute them to the first teacher account, if
-- one exists. Deliberately not enforced with NOT NULL: on a database with
-- grades but zero teacher accounts this subquery yields NULL for every row,
-- and a NOT NULL constraint would then fail the migration outright and take
-- the app down. The application (Grade#teacher / TeacherController) already
-- guarantees every newly created grade has a teacher; this column only stays
-- nullable to tolerate unattributable legacy data.
UPDATE grades SET teacher_id = (SELECT id FROM users WHERE role = 'TEACHER' ORDER BY id LIMIT 1) WHERE teacher_id IS NULL;

ALTER TABLE grades ALTER COLUMN created_at DROP DEFAULT;

CREATE INDEX idx_grades_teacher_id ON grades(teacher_id);
