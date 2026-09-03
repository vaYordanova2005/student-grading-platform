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
* **Teacher** — must end in `@uni-sofia.bg` (validated with a regex in
  `UserValidationService`)
* **Student** — any valid email, no domain restriction

Password for both roles: **at least 5 characters**, no format restrictions (not EGN, not
digits-only).

## Demo data

`SEED_DEMO_DATA=true` on first startup creates:

* 8 teachers: `teachera@uni-sofia.bg` … `teacherh@uni-sofia.bg`
* 20 students: `student1@test.com` … `student20@test.com`
* ~400 grades across 8 subjects

## Passwords

**All accounts (admin, all teachers, all students) use the same password:
`password12345`.** No exceptions. If a new account is added manually through the admin
panel, give it the same password unless explicitly asked otherwise.

## Admin credentials

`admin` / `password12345` (see `SEED_ADMIN_USERNAME` / `SEED_ADMIN_PASSWORD` in
`backend/.env` for the real value if someone changed it locally).
