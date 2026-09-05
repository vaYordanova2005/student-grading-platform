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

Stateless JWT, delivered in an **httpOnly cookie** (`markly_token`) rather than to
JavaScript — an XSS hole in the SPA can then use the session while the page is open, but
cannot read the token out and keep it. A token is issued once at login (`JwtService`,
`JWT_EXPIRATION_MINUTES`, default 480 = 8h) and must be re-obtained by logging in again
after it expires; there is no `/api/auth/refresh` or similar. `GET /api/auth/me` is how
the SPA recovers who it is after a reload, since it cannot read the cookie itself.

Because the browser now attaches the cookie to cross-site requests too, CSRF protection
is required. Spring's own CSRF filter is session-based and this app is stateless, so the
check lives in `JwtAuthenticationFilter`: every non-GET request must carry an
`X-CSRF-Token` header equal to `HMAC(jwt-secret, "csrf:" + jti)`, a value the SPA gets
with the session and a foreign site cannot compute. On Render the SPA and the API are
separate hosts, so the cookie needs `SameSite=None; Secure` (`AUTH_COOKIE_SAME_SITE`,
`AUTH_COOKIE_SECURE`); locally over plain http it stays `Lax`. CORS allows exactly one
origin (`FRONTEND_ORIGIN`/`cors.allowed-origin`), not a wildcard, with credentials
enabled.

A JWT cannot be withdrawn, so revocation is expressed as `users.token_version`: it is
copied into every token and re-checked on every request, and bumping it (logout,
deactivation) retires every outstanding token for that account at once.

## Brute-force protection and password rules

`LoginRateLimitFilter` caps **failed** login attempts per client address (15 per 5
minutes → 429). Successful logins are not counted, so a lab behind one NAT address
cannot lock itself out by signing in normally. The address comes from the *last* entry of
`X-Forwarded-For` (`ClientIp`) — proxies append rather than replace, so the leftmost
entry is caller-controlled and using it would let an attacker mint a fresh bucket per
request; this assumes exactly one trusted proxy in front of the app.

`LoginAttemptService` locks an account for 15 minutes after 5 consecutive failures (→
423) and writes every login outcome to the `com.markly.audit` logger. The rate-limit
counters are in-memory, which is enough for the single instance this is deployed as — a
multi-instance deployment would need them shared (Redis) or enforced at the edge.

Passwords must be at least 10 characters with an upper-case letter, a lower-case letter
and a digit, must not contain the username's local part, and must not be one of the
common passwords listed in `UserValidationService`. The rule covers the seeded accounts
too: `DataSeeder` refuses to start if `SEED_ADMIN_PASSWORD` fails it (note that with the
default username `admin`, the password may not contain "admin"), and the demo constants
are asserted against it in `SeedPasswordPolicyTest`.

`POST /api/auth/password` lets any signed-in user rotate their own password: the current
password is required, the new one goes through the same policy, and the token version is
bumped so every other session ends while the calling tab gets a fresh cookie. This is
also the only way to rotate the seeded admin password from inside the app.

## No self-service or admin account management beyond create

An admin can list users (`GET /api/admin/users`) and create one
(`POST /api/admin/users`) — that's the entire user-management surface. An admin can also
deactivate/reactivate an account (`PUT /api/admin/users/{id}/status`) and lift a
brute-force lockout (`POST /api/admin/users/{id}/unlock`). There is still no edit or
delete endpoint for an account, and no self-service password reset or change for any
role (admin, teacher, or student) — the only way to change a password is for an admin to
create a new account with one. This is a deliberate scope cut, not an oversight; don't
assume a missing edit/delete endpoint is a bug to silently "fix."

## Demo data

`SEED_DEMO_DATA=true` on first startup creates:

* 8 teachers: `teacher1@uni-sofia.bg` … `teacher8@uni-sofia.bg`
* 20 students: `student1@uni-sofia.bg` … `student20@uni-sofia.bg`, each with a
  `student_profiles` row and grades across every semester up to and including the one
  they're currently enrolled in (~400 `grades` rows total for the fixed random seed)
* A handful of `calendar_events` (one test per subject, a holiday, a career-day event)

## Passwords

**All accounts (admin, all teachers, all students) use the same password:
`Demo-Markly2024`.** No exceptions. If a new account is added manually through the admin
panel, give it the same password unless explicitly asked otherwise.

## Admin credentials

`admin` / `Demo-Markly2024` (see `SEED_ADMIN_USERNAME` / `SEED_ADMIN_PASSWORD` in
`backend/.env` for the real value if someone changed it locally).
