-- The username already is the student's email, so a separate "personal
-- email" field on the profile was redundant and confusing in the UI.
ALTER TABLE student_profiles DROP COLUMN personal_email;
