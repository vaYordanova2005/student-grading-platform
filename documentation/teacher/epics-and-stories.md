# Teacher — Epics & User Stories

Product backlog for the **Teacher** role, modeled loosely on what platforms like Shkolo
offer, scoped down to what actually makes sense for Markly.

**Currently implemented (MVP):** a teacher logs in and enters a grade for a student by
the student's email (`POST /api/teacher/grades`, `TeacherDashboard.tsx`). The grade is
automatically stamped with the entering teacher (`teacher_id`) and a timestamp
(`created_at`). There is no way yet for a teacher to see, edit, or delete grades they've
already entered — the only backend operation is creating a new one. Everything else below
is proposed backlog, not built yet.

## Epic 1 — Grade entry & management

The core of the teacher's job today; still missing basic read/edit/delete.

* **As a teacher, I want to enter a grade for a student by their email, so that it's
  recorded in the system.** *(implemented)*
* **As a teacher, I want to see a list of grades I've entered, so that I can check my own
  work without asking the student or admin.**
* **As a teacher, I want to edit a grade I entered, so that I can fix a typo (wrong
  number, wrong subject) without creating a duplicate.**
* **As a teacher, I want to delete a grade I entered by mistake, so that it doesn't stay
  in the student's record.**
* **As a teacher, I want to enter grades for multiple students at once (e.g. a whole
  class after a test), so that I don't repeat the same form dozens of times.**
* **As a teacher, I want student email autocomplete/lookup when entering a grade, so that
  I don't mistype an email and grade the wrong student.**

## Epic 2 — Class roster

Today a teacher has no view of "my students" — grade entry works purely by typing a known
email, with no class/subject assignment backing it.

* **As a teacher, I want to see a list of students in a class I teach, so that I don't
  need to already know every student's email by heart.**
* **As a teacher, I want to see only the subjects/classes assigned to me, so that I can't
  accidentally grade a student who isn't mine.** (Depends on the academic-structure work
  described in [`../admin/epics-and-stories.md`](../admin/epics-and-stories.md).)

## Epic 3 — Attendance

* **As a teacher, I want to mark a student absent (excused or unexcused) for my class, so
  that attendance is tracked alongside grades.**
* **As a teacher, I want to see attendance history for a student in my subject, so that I
  have context before a parent conversation.**

## Epic 4 — Homework

* **As a teacher, I want to post a homework assignment for a subject/class, so that
  students know what's expected of them.**
* **As a teacher, I want to see which students have submitted a given assignment, so
  that I know who's behind.**

## Epic 5 — Communication

* **As a teacher, I want to attach an optional comment to a grade (e.g. "great
  improvement" or "needs to review chapter 3"), so that the student gets context, not
  just a number.**
* **As a teacher, I want to post an announcement visible to my class, so that I can share
  information (test date, materials) without a separate channel.**
* **As a teacher, I want to message a student directly, so that I can follow up on
  something without needing their personal contact info.**

## Epic 6 — Account & profile

* **As a teacher, I want to log in securely, so that only I can enter grades under my
  name.** *(implemented — JWT login, shared across roles, `@uni-sofia.bg` email required)*
* **As a teacher, I want to change my own password, so that I'm not stuck with whatever
  the admin set when creating my account.**
* **As a teacher, I want to see my own profile info (name, email, assigned subjects), so
  that I can confirm my account is set up correctly.**

## Epic 7 — Reporting

* **As a teacher, I want to export the grades I've entered for a class/subject (CSV), so
  that I can share them outside the app (e.g. with the school office).**
* **As a teacher, I want to see the average grade for a class I teach, so that I can
  gauge how the class is doing as a whole, not just student by student.**

## Suggested priority for next iteration

1. Epic 1 gaps (view/edit/delete own grades) — the most obvious missing piece; grade
   entry without the ability to fix a mistake is a real gap today, and needs no new
   schema (`teacher_id` already scopes "my grades").
2. Epic 6 (own password change) — small, same pattern as the student/admin version of
   this story.
3. Epic 5's grade comment — cheap addition to the existing `grades` table (one nullable
   text column), high value for the student-facing side too.
4. Epic 2 (class roster) and Epic 7 (reporting) — depend on the academic-structure epic
   in the admin backlog (subjects/classes as real entities, not free text); worth doing
   together with that work rather than before it.
5. Epics 3 and 4 (attendance, homework) — new domain entities, same scope caveat as the
   equivalent items in the student and admin backlogs.
