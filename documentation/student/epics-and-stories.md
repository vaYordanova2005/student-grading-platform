# Student — Epics & User Stories

Product backlog for the **Student** role, modeled loosely on what platforms like Shkolo
offer, scoped down to what actually makes sense for Markly.

**Currently implemented (MVP):** a student logs in and sees their own grades, grouped by
semester and subject (`GET /api/student/grades`, `StudentDashboard.tsx`). Everything else
below is proposed backlog, not built yet.

## Epic 1 — Grades & performance

The core reason a student opens the app: see how they're doing.

* **As a student, I want to see all my grades grouped by subject and semester, so that I
  can find a specific grade quickly.** *(implemented)*
  * Acceptance: grades sorted by semester then subject, matches `GradeResponse`.
* **As a student, I want to see my average grade per subject, so that I know where I
  stand without doing the math myself.**
* **As a student, I want to see my overall average for the semester/year, so that I have
  a single number that summarizes my performance.**
* **As a student, I want to see a simple trend (e.g. a small chart) of my grades over
  time per subject, so that I can tell if I'm improving or slipping.**
* **As a student, I want to filter/search my grades by subject or semester, so that I
  don't have to scroll through everything.**

## Epic 2 — Notifications on new grades

Right now a student only finds out about a new grade by opening the app and checking.

* **As a student, I want to be notified (in-app, badge or list) when a teacher enters a
  new grade for me, so that I don't have to keep refreshing to find out.**
* **As a student, I want new grades to be visually marked as "new" until I've seen them,
  so that I know what changed since my last visit.**

## Epic 3 — Timetable

* **As a student, I want to see my weekly class schedule, so that I know where I need to
  be and when.**
* **As a student, I want to see upcoming exam/test dates, so that I can plan my studying.**

## Epic 4 — Attendance

* **As a student, I want to see my own absences per subject, marked as excused or
  unexcused, so that I know if I'm at risk of a problem.**
* **As a student, I want to be notified when a new absence is recorded for me, so that I
  find out the same day, not weeks later.**

## Epic 5 — Homework

* **As a student, I want to see homework/assignments a teacher has posted per subject,
  so that I know what's due and when.**
* **As a student, I want to mark an assignment as done (or upload a file for it), so that
  my teacher knows I've submitted it.**

## Epic 6 — Communication

* **As a student, I want to see announcements posted by teachers or the school, so that I
  don't miss important information.**
* **As a student, I want to see any comment/feedback a teacher attached to a specific
  grade, so that I understand *why* I got that grade, not just the number.**
* **As a student, I want to send a message to one of my teachers, so that I can ask a
  question without needing another channel (email, etc).**

## Epic 7 — Account & profile

* **As a student, I want to log in securely with my email and password, so that only I
  can see my own data.** *(implemented — JWT login, shared across roles)*
* **As a student, I want to change my own password, so that I'm not stuck with whatever
  the admin/teacher set for me.**
* **As a student, I want to see my own profile info (name, email), so that I can confirm
  my account details are correct.**

## Epic 8 — Reports

* **As a student, I want to download a term report card (e.g. PDF) with all my grades for
  the semester, so that I have something I can print or share with a parent.**

## Suggested priority for next iteration

1. Epic 1 (averages/trend) — builds directly on data that already exists (`grades`
   table), no new schema needed.
2. Epic 7 (change own password) — small, closes an obvious gap, no new entities.
3. Epic 2 (new-grade notifications) — high value, moderate effort (needs a "seen" flag or
   timestamp comparison).
4. Epics 3–6, 8 — each needs new domain entities (timetable, absences, homework,
   messages) and is a bigger scope decision, not just a Student-role addition.
