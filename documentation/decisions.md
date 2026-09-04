# Project decisions (Markly)

This file locks in decisions made for the project so they aren't accidentally revisited or
reversed. If something here needs to change, change it deliberately and update this file
immediately.

## Java version

**Java 21.** Not 25. `backend/pom.xml` → `<java.version>21</java.version>`.

## Username scheme

Both roles (teacher, student) use **email** as the username. No faculty numbers, no
national ID (EGN) — nowhere in the active code (`backend/`, `frontend/`). EGN only exists
in `legacy/` (the old TCP socket version, unmaintained, do not touch).

* **Admin** — `admin` (fixed username)
* **Teacher** — must end in `@uni-sofia.bg`, local part letters optionally followed by
  digits (validated with a regex in `UserValidationService`)
* **Student** — any valid email, no domain restriction

Password for both roles: **at least 5 characters**, no format restrictions (not EGN, not
digits-only).

## Grading scale

Grades are **2–6** (Bulgarian school scale), enforced with `@Min`/`@Max` wherever a grade
is written (`CreateGradeRequest`). **2 is the failing grade** (`FAIL_GRADE` in both
`DemoDataSeeder` and `frontend/src/utils/grades.ts`), not 1 — there is no 1 anywhere in
the system. 6 is the top grade (`TOP_GRADE` on the frontend). Not a 1–5 or A–F scale;
don't assume otherwise when adding grade-related logic.

## Semester range

Semesters are **1–8** (a 4-year bachelor's program, 2 semesters/year) — enforced with
`@Min(1)`/`@Max(8)` on `CreateGradeRequest.semester` and
`UpsertStudentProfileRequest.enrolledSemester`, and `@Min(0)`/`@Max(8)` on
`completedSemester` (0 meaning "not yet completed any"). The frontend's `SEMESTERS`
constant (`JournalPage.tsx`, `StatisticsPage.tsx`) mirrors this. Not configurable per
program — there's no concept of a shorter/longer degree in the data model.

## Auth model

Stateless JWT only — no server-side sessions, no cookies, no refresh tokens. A token is
issued once at login (`JwtService`, `JWT_EXPIRATION_MINUTES`, default 480 = 8h) and must
be re-obtained by logging in again after it expires; there is no `/api/auth/refresh` or
similar. CSRF protection is deliberately disabled in `SecurityConfig`
(`csrf(csrf -> csrf.disable())`) — safe *because* auth is a Bearer token, not a cookie;
if cookie-based auth is ever introduced, CSRF protection has to come back with it. CORS
allows exactly one origin (`FRONTEND_ORIGIN`/`cors.allowed-origin`), not a wildcard.

## No self-service or admin account management beyond create

An admin can list users (`GET /api/admin/users`) and create one
(`POST /api/admin/users`) — that's the entire user-management surface. There is no edit,
deactivate, or delete endpoint for any account, and no self-service password reset or
change for any role (admin, teacher, or student) — the only way to change a password is
for an admin to create a new account with one. This is a deliberate scope cut, not an
oversight; don't assume a missing edit/delete endpoint is a bug to silently "fix."

## Demo data

`SEED_DEMO_DATA=true` on first startup creates:

* 8 teachers: `teacher1@uni-sofia.bg` … `teacher8@uni-sofia.bg`
* 20 students: `student1@uni-sofia.bg` … `student20@uni-sofia.bg`, each with a
  `student_profiles` row and grades across every semester up to and including the one
  they're currently enrolled in (~400 `grades` rows total for the fixed random seed)
* A handful of `calendar_events` (one test per subject, a holiday, a career-day event)

## Passwords

**All accounts (admin, all teachers, all students) use the same password:
`password12345`.** No exceptions. If a new account is added manually through the admin
panel, give it the same password unless explicitly asked otherwise.

## Admin credentials

`admin` / `password12345` (see `SEED_ADMIN_USERNAME` / `SEED_ADMIN_PASSWORD` in
`backend/.env` for the real value if someone changed it locally).
