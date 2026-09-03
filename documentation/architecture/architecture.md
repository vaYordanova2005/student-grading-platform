# Architecture (Markly)

A school grading system: Spring Boot REST backend + PostgreSQL + React (TypeScript)
frontend. Three roles — Admin, Teacher, Student — with JWT authentication.

The old TCP socket version lives in [`legacy/`](../../legacy/) — reference only, not
touched, not part of the current architecture.

## Overview

```
frontend (React + Vite, TS)  --HTTP/JSON + JWT-->  backend (Spring Boot)  -->  PostgreSQL
```

* The frontend holds no business logic — it calls the REST API and renders the response.
* The backend is the single source of truth: validation, authorization, data access.
* No separate API gateway / microservices — a monolithic Spring Boot application.

## Backend (`backend/src/main/java/com/markly/backend/`)

| Package | Responsibility |
|---|---|
| `web/` | REST controllers — `AuthController`, `AdminController`, `TeacherController`, `StudentController`. Thin layer: accept DTOs, call service/repository, return DTOs. |
| `web/dto/` | Request/response DTOs (e.g. `CreateUserRequest`, `GradeResponse`, `LoginResponse`). The backend never serializes entities directly to clients. |
| `domain/` | JPA entities — `User`, `Grade`, `Role` (enum: ADMIN/TEACHER/STUDENT). |
| `repository/` | Spring Data JPA repositories — `UserRepository`, `GradeRepository`. |
| `service/` | Business/validation logic outside the controllers — `UserValidationService` (username/email rules, see [`decisions.md`](decisions.md)). |
| `security/` | JWT — `JwtService` (issues/validates tokens), `JwtAuthenticationFilter` (reads `Authorization: Bearer`), `AppUserDetailsService` + `AppUserPrincipal` (Spring Security user model). |
| `config/` | `SecurityConfig` (filter chain, roles per endpoint), `DataSeeder` (admin account on startup), `DemoDataSeeder` (demo teachers/students/grades when `SEED_DEMO_DATA=true`). |
| `exception/` | `ApiExceptionHandler` (`@ControllerAdvice`) + `ApiError` — a single error format across all endpoints. |

### Authentication / authorization

* Login → `AuthController` → issues a JWT (`JwtService`), signed with `JWT_SECRET`.
* Every subsequent request carries the JWT in the `Authorization` header;
  `JwtAuthenticationFilter` validates it and puts an `Authentication` in the
  `SecurityContext`.
* Role-based access is enforced in `SecurityConfig` (endpoint → role) and additionally in
  the controllers where needed (e.g. a teacher can only see/write their own grades).

### Database

See [`../database/`](../database/) for schema and migrations. Managed with Flyway
(`backend/src/main/resources/db/migration/`) — versions apply automatically on startup,
never manual ALTERs in production.

## Frontend (`frontend/src/`)

| Folder | Responsibility |
|---|---|
| `api/` | HTTP client to the backend (fetch/axios wrapper), attaches the JWT to requests. |
| `auth/` | Login state, token storage, context for the current user/role. |
| `routes/` | React Router config, route guards per role (admin/teacher/student). |
| `pages/` | Pages per role (admin panel, teacher grade entry, student view). |
| `components/` | Reusable UI components. |
| `hooks/` | Custom React hooks. |
| `types/` | Shared TypeScript types (mirroring the backend DTOs). |

## Roles and access

* **Admin** (`admin`, fixed username) — creates teacher/student accounts.
* **Teacher** (email, must end in `@uni-sofia.bg`) — enters grades for students by email.
* **Student** (any email) — sees only their own grades.

Full username/password rules: [`decisions.md`](decisions.md).

## Environments / configuration

Everything configurable goes through env vars (see [README.md](../../README.md) in the
repo root) — no hardcoded credentials or connection strings in the code.
