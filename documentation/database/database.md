# Database schema (Markly)

PostgreSQL. Schema is owned by Flyway migrations in
[`backend/src/main/resources/db/migration/`](../../backend/src/main/resources/db/migration/)
— never edit the schema by hand, add a new migration instead. JPA entities live in
[`backend/src/main/java/com/markly/backend/domain/`](../../backend/src/main/java/com/markly/backend/domain/).

## Tables

### `users`

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `username` | `VARCHAR(255)` | `NOT NULL`, `UNIQUE` — an email address for all roles, see [`../architecture/decisions.md`](../architecture/decisions.md) |
| `password` | `VARCHAR(255)` | `NOT NULL` — hashed, never plaintext |
| `role` | `VARCHAR(20)` | `NOT NULL`, `CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT'))` |

### `grades`

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `student_id` | `BIGINT` | `NOT NULL`, FK → `users(id)`, indexed (`idx_grades_student_id`) |
| `teacher_id` | `BIGINT` | FK → `users(id)`, indexed (`idx_grades_teacher_id`). Nullable at the DB level (see below), but the application always sets it — `Grade#teacher` is mapped `optional = false` |
| `subject` | `VARCHAR(255)` | `NOT NULL` |
| `semester` | `INTEGER` | `NOT NULL` |
| `grade` | `INTEGER` | `NOT NULL` |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` (no default — set by the app on insert via `@PrePersist`) |

## Relationships

```
users (1) ──< grades.student_id   one student has many grades
users (1) ──< grades.teacher_id   one teacher has entered many grades
```

Both `student_id` and `teacher_id` reference the same `users` table; the role column is
what distinguishes a "student" row from a "teacher" row — there is no separate students
or teachers table.

## Why `teacher_id` is nullable at the DB level

`teacher_id` was added after `grades` already existed in production (`V2`). Rows created
before that migration have no real record of who entered them, so a `NOT NULL` constraint
on `teacher_id` would either:

* fail the migration outright on any database that had grades but zero teacher accounts
  (the backfill subquery returns `NULL` for every row), or
* require inventing a fake attribution for old data.

`V2` backfills `teacher_id` best-effort (first teacher account, if one exists) but does
**not** enforce `NOT NULL` on the column. `V3` exists to converge two divergent schema
states: an early edit of `V2` briefly shipped with `SET NOT NULL`, so databases that
applied that version still carry the constraint while fresh databases don't — `V3` drops
it unconditionally (a no-op where it's already absent) so every environment ends up with
the same schema.

The application-level guarantee (every *new* grade always has a teacher) lives in
`Grade#teacher` (`optional = false`) and `TeacherController`, not in the database
constraint. Any code that reads `teacher_id` back must null-check rather than assume,
since legacy/unattributable rows can still be `NULL`.

## Migration history

| Version | File | Change |
|---|---|---|
| V1 | `V1__init.sql` | Creates `users` and `grades`, index on `grades.student_id`. |
| V2 | `V2__add_grade_audit_columns.sql` | Adds `teacher_id` (nullable, best-effort backfilled) and `created_at` (`NOT NULL`, default dropped after backfill) to `grades`; index on `teacher_id`. |
| V3 | `V3__relax_grade_teacher_not_null.sql` | Drops `NOT NULL` on `grades.teacher_id` to converge databases that picked up an early, since-reverted version of V2. |

## Seeding

* `DataSeeder` — creates the admin account on first startup.
* `DemoDataSeeder` — with `SEED_DEMO_DATA=true`, creates 8 teachers, 20 students, and
  ~400 grades across 8 subjects. Local/test use only, see
  [`../architecture/decisions.md`](../architecture/decisions.md).
