# Markly

School system: Spring Boot REST backend + PostgreSQL + React frontend. Roles Admin /
Teacher / Student — admin creates users, teacher enters grades, student views their own.

The original TCP socket version of the project is kept in [legacy/](legacy/) as a
reference only and is no longer maintained.

See [documentation/](documentation/) for architecture, decisions, and per-role docs.

## Stack

* Backend: Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA, Flyway, Maven
* Frontend: React, TypeScript, Vite, React Router
* Database: PostgreSQL

## Running locally

### 1. PostgreSQL

Create a database (defaults to `markly`):

```
createdb markly
```

### 2. Backend

```
cd backend
DB_USERNAME=postgres DB_PASSWORD=<your password> \
JWT_SECRET=<random string, at least 32 bytes> \
SEED_ADMIN_PASSWORD=<password for the admin account> \
./mvnw spring-boot:run
```

`JWT_SECRET` and `SEED_ADMIN_PASSWORD` have no default value — the app won't start
without them, so it can't accidentally end up deployed with publicly known credentials.
On first startup Flyway creates the schema and seeds an admin account with username
`admin` (configurable via `SEED_ADMIN_USERNAME`) and the password set in
`SEED_ADMIN_PASSWORD`.

Configurable env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`,
`DB_SSLMODE`, `SERVER_PORT`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `SEED_ADMIN_USERNAME`,
`SEED_ADMIN_PASSWORD`, `SEED_DEMO_DATA`, `FRONTEND_ORIGIN`.

#### Demo data (optional)

With `SEED_DEMO_DATA=true`, the first startup against an empty database creates 8
teachers, 20 students, and sample grades, so there's something to look at on the
dashboards. Disabled by default — only enable it on a local/test database, never against
a live production database. Demo credentials (valid under the same rules as manually
created accounts):

* Teachers: `teachera@uni-sofia.bg` … `teacherh@uni-sofia.bg`, password `password12345`
* Students: `student1@test.com` … `student20@test.com`, password `password12345`

For a remote database (e.g. [Neon](https://neon.tech)), set `DB_HOST` to the host from
the connection string and `DB_SSLMODE=require`:

```
DB_HOST=<ep-xxx.neon.tech> DB_NAME=<database> DB_USERNAME=<user> DB_PASSWORD=<password> DB_SSLMODE=require ./mvnw spring-boot:run
```

### 3. Frontend

```
cd frontend
npm install
npm run dev
```

Expects the backend on `http://localhost:8080` by default (see `frontend/.env`).

## Roles

* **Admin** → creates teacher/student accounts (teacher username: `@uni-sofia.bg` email;
  student: any email, password at least 5 characters)
* **Teacher** → enters student grades by email
* **Student** → views their own grades
