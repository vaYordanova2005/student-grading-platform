# Student — What the system does

This describes the **Student** role as it actually exists in the codebase today (not a
backlog or aspiration). A student account is created by an admin
(`POST /api/admin/users`); a student cannot self-register.

## Login

`POST /api/auth/login` — email/username + password, validated by Spring Security against
the `users` table. On success the server issues a JWT (`AuthController`,
`web/dto/LoginResponse`) containing the username and role; the frontend stores it and
attaches it as a Bearer token to every subsequent request. There is no "forgot password"
or self-service password change — only an admin can set a student's password (via account
creation; there's no reset endpoint either, for any role).

## Navigation

Once logged in, a student sees four sections in the top nav (`routes/Layout.tsx`):
**Начало** (dashboard), **Дневник** (journal), **Статистики** (statistics), **Календар**
(calendar), plus a profile link under their username. `JournalPage` and `StatisticsPage`
render "under construction" for any non-STUDENT role, and — since both routes are
student-only in practice — the nav only offers those two items to STUDENT accounts;
other roles see just Начало and Календар.

## Dashboard (`/student`, `StudentDashboard.tsx`)

Landing page after login. Pulls all of the student's grades
(`GET /api/student/grades`) and shows:

* Four stat tiles: overall average, total grade count, count of grades that are `6`
  ("Отлични"), and number of distinct subjects.
* **Успех по предмети** — a horizontal bar per subject, average grade + grade count,
  colored by tier (green ≥5.5, blue ≥4.5, red below).
* **Развитие по семестри** — one pill per semester showing that semester's average, only
  shown once the student has grades in more than one semester.

## Journal (`/journal`, `JournalPage.tsx`)

Semesters 1–8 are always rendered as collapsible cards (the student's own
`enrolledSemester`, from their profile, is labeled "текущ"); any grade whose semester
falls outside that range — only possible via a legacy row or a direct database write,
since the backend validates 1–8 on every write — gets its own extra card appended after
them rather than being silently dropped. Inside each semester, grades are grouped by
subject into one row per subject, with every grade for that subject shown as a clickable
pill. Clicking a grade expands a detail row with:

* the date it was recorded,
* whether it's a **regular** or **retake** session grade — inferred client-side
  (`utils/grades.ts: classifySessionTypes`): within a semester+subject, the first grade
  chronologically is "редовна сесия", every later one for the same subject is
  "поправителна сесия", regardless of the grade value (there's no real session-type field
  in the database),
* the teacher who entered it.

## Statistics (`/statistics`, `StatisticsPage.tsx`)

A deeper analytical view over the same grade data:

* Stat tiles: overall average, total grade count, best-performing subject, retake count.
* **Разпределение на оценките** — bar chart of how many grades fall on each value 2–6.
* **Тенденция по семестри** — an actual SVG line chart of the semester averages (only
  shown with grades in 2+ semesters); the x-axis is normally 1–8 but stretches to fit
  any semester outside that range too, the same overflow case the journal handles.
* **По предмети** — a subject × semester matrix (one row per subject, one column per
  semester with grades in it) with a per-subject overall average and an up/flat/down
  trend arrow comparing the first vs. last measured semester (>0.25 difference to count
  as a real trend).
* **Редовна срещу поправителна сесия** — average grade in regular sessions vs. retakes,
  shown only if the student has at least one retake.

## Profile (`/profile`, `ProfilePage.tsx`)

`GET /api/student/profile` returns registrar-style data from the `student_profiles`
table (`StudentProfile` entity) — admin-managed, not editable by the student:

ОКС (degree level), факултетен номер, факултет, специалност, вид обучение,
специализация, група, вид прием, състояние, записан семестър, заверен семестър, поток,
plus the student's email/username. Any field the admin hasn't filled in shows as `—`. If
no `StudentProfile` row exists yet for the student, the endpoint returns an all-empty
response rather than an error.

## Calendar (`/calendar`, `CalendarPage.tsx`)

Shared across all roles (`GET /api/calendar/events`, no auth restriction on reads) — a
student sees the same calendar as teachers/admins but **read-only**: the "add to
calendar" form and delete buttons only render for ADMIN/TEACHER
(`user.role === 'ADMIN' || 'TEACHER'`; enforced again server-side in `SecurityConfig` for
writes). Students can:

* Browse a month grid with a day/year picker, jump to today.
* Click a day to see every event on it.
* See an "Предстоящи" (upcoming) list of all future events.

Each event is one of three types (`CalendarEventType`): **TEST** (tied to a subject),
**HOLIDAY**, or **EVENT** — single-day or a date range, each with a title, optional
description, and the name of who created it.

## What a student cannot do (no backend support at all)

No entities or endpoints exist for any of the following, so these aren't just missing UI —
there's nothing to build on top of yet: weekly class timetable, absences/attendance,
homework/assignments, announcements, per-grade teacher comments, messaging a teacher, or
downloadable report cards/PDF export. Grade filtering/search and student self-service
password change are also not implemented.

## Data model touching the student role

* **`User`** — `username` (email), `password` (hashed), `role`. No separate "name" field
  exists anywhere in the system; students are identified by username/email only.
* **`Grade`** — student, teacher, subject, semester (int), grade (int), `createdAt`.
  Session type (regular/retake) is *not* stored — it's inferred on the frontend as
  described above.
* **`StudentProfile`** — one-to-one with `User`, all registrar fields listed under
  Profile above. Optional; only meaningful for STUDENT-role users.
* **`CalendarEvent`** — type, title, description, optional subject (TEST only), start/end
  date, creator.
