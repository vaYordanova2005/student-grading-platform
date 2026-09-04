# Admin — Epics & User Stories

Product backlog for the **Admin** role, modeled loosely on what platforms like Shkolo
offer, scoped down to what actually makes sense for Markly.

**Currently implemented (MVP):** an admin logs in, sees a list of all users
(`GET /api/admin/users`), and creates a new teacher/student/admin account
(`POST /api/admin/users`, validated by `UserValidationService`). No editing, deactivation,
or deletion of users yet. Everything else below is proposed backlog, not built yet.

## Epic 1 — User management

The core of the admin's job today; still has obvious gaps.

* **As an admin, I want to see a list of all users with their role, so that I have an
  overview of everyone in the system.** *(implemented)*
* **As an admin, I want to create a teacher, student, or admin account, so that new
  people can log in.** *(implemented — username/password rules enforced server-side)*
* **As an admin, I want to edit a user's username (email), so that I can fix a typo or
  handle an email change without deleting and recreating the account.**
* **As an admin, I want to reset a user's password, so that I can help someone who's
  locked out without needing database access.**
* **As an admin, I want to deactivate or delete a user, so that people who left the
  school lose access.**
* **As an admin, I want to search/filter the user list by name, role, or email, so that I
  can find one account quickly once there are hundreds of users.**
* **As an admin, I want to bulk-import students/teachers from a CSV, so that I don't have
  to create accounts one by one at the start of a school year.**

## Epic 2 — Academic structure

Today there is no concept of a class, subject list, or school year in the data model —
`grades.subject` is a free-text string entered by whichever teacher creates the grade.

* **As an admin, I want to maintain a fixed list of subjects, so that teachers pick from
  a dropdown instead of typing free text (avoiding "Math" vs "Maths" vs "math").**
* **As an admin, I want to define classes (e.g. "10A") and assign students to them, so
  that reporting can be grouped by class, not just by individual student.**
* **As an admin, I want to assign teachers to the subjects/classes they teach, so that a
  teacher only enters grades for their own students, not anyone in the school.**
* **As an admin, I want to define the school year and its semesters/terms, so that grade
  entry and reports are scoped to the right period automatically.**

## Epic 3 — Oversight & reporting

Right now an admin has no visibility into grades at all — only `TeacherController` and
`StudentController` touch `grades`.

* **As an admin, I want to see school-wide grade statistics (averages per subject, per
  class), so that I can spot subjects or classes that are struggling.**
* **As an admin, I want to see an audit trail of who entered or changed a grade and when,
  so that I can investigate a dispute (grade already has `teacher_id` + `created_at` to
  build this on).**
* **As an admin, I want to export a report (CSV/PDF) of grades for a class or the whole
  school, so that I can share it outside the app.**

## Epic 4 — Announcements

* **As an admin, I want to post a school-wide announcement, so that all
  teachers/students see important information (holidays, schedule changes, etc).**

## Epic 5 — Account & security

* **As an admin, I want to log in securely, so that only authorized staff can manage
  accounts.** *(implemented — JWT login, shared across roles)*
* **As an admin, I want to change my own password, so that I'm not stuck with the seeded
  `SEED_ADMIN_PASSWORD` forever.**
* **As an admin, I want to see a log of admin actions (who created/edited/deleted which
  account), so that changes to sensitive accounts are traceable.**

## Epic 6 — System configuration

* **As an admin, I want to toggle demo data seeding from the UI instead of an env var, so
  that I don't need backend access to set up a demo/training environment.**
* **As an admin, I want to configure password policy (minimum length, etc.) instead of it
  being hardcoded, so that requirements can change without a code deploy.**

## Suggested priority for next iteration

1. Epic 1 gaps (edit, reset password, deactivate/delete) — closes obvious holes in
   already-implemented functionality, no new schema.
2. Epic 5 (admin's own password change) — small, same pattern as the student version of
   this story.
3. Epic 3 (oversight/reporting) — high value, and the data (`teacher_id`, `created_at`)
   already exists to build a basic version without new entities.
4. Epic 2 (academic structure: subjects, classes, school year) — the biggest scope
   decision here; it changes the data model (`grades.subject` from free text to a
   reference) and touches Teacher and Student flows too, not just Admin.
5. Epics 4 and 6 — nice-to-have, lowest urgency relative to the above.
