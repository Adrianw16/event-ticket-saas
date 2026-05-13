# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

```bash
# Run the application
./mvnw spring-boot:run

# Build (skip tests)
./mvnw package -DskipTests

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=SaasApplicationTests

# Start the PostgreSQL container (required before running the app)
docker run --name event-ticket-postgres -e POSTGRES_DB=event_ticket_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -p 5433:5432 -d postgres:16
```

## Architecture

Spring Boot 3.5 + Java 21 REST API backed by PostgreSQL (via Docker on port 5433). All classes live flat in `com.eventticket.saas` — no sub-packages yet.

**Request flow**: HTTP request → `@RestController` → `JpaRepository` → PostgreSQL

**Key constraints**:
- `spring.jpa.hibernate.ddl-auto=none` — Hibernate never touches the schema. All schema changes go through Flyway migrations in `src/main/resources/db/migration/` using the `V{n}__{description}.sql` naming convention.
- Spring Security is present but fully open (`anyRequest().permitAll()`, CSRF disabled) — authentication is not yet implemented.
- Entities use Lombok `@Data`. Jackson serialization of lazy-loaded JPA relations will fail — use `@JsonIgnore` on `@ManyToOne(fetch = LAZY)` fields, or return DTOs instead of raw entities.

## Data Model

- `users` — `id`, `email`, `password_hash`, `created_at`, `org_id` (FK → organizations)
- `organizations` — `id`, `name`, `slug`, `email`, `created_at`
- A user belongs to one organization; org side has no back-reference yet.

## Database

- Host: `localhost:5433` (Docker-mapped)
- Database: `event_ticket_db`
- Credentials in `src/main/resources/application.properties` (not in `.env`)

## What Has Been Built

### Completed
- Spring Boot scaffold with Web, JPA, PostgreSQL, Security, Validation, Flyway, Actuator
- `GET /hello` — smoke test endpoint
- `User` entity + `UserRepository` + `UserController` (`GET /users`, `POST /users`)
- Flyway V1: creates `users` table
- `Organization` entity + `OrganizationRepository` + `OrganizationController` (`GET /organizations`, `POST /organizations`)
- Flyway V2: creates `organizations` table, adds `org_id NOT NULL FK` to `users` — this is the multi-tenancy foundation
- `SecurityConfig`: Spring Security wired but fully open (all endpoints permit all, CSRF disabled)

### Known Design Decisions
- `User.organization` is `@ManyToOne(fetch = LAZY)` with `@JsonIgnore` — the organization is intentionally excluded from `/users` responses to avoid Hibernate proxy serialization errors. To expose org data for a user, add a dedicated endpoint (e.g. `GET /users/{id}/organization`) or use a DTO.
- No authentication implemented yet — `SecurityConfig` is a placeholder for future JWT or session-based auth.
- No sub-packages yet — all classes are flat in `com.eventticket.saas`. As the codebase grows, expect to split into `entity`, `controller`, `repository`, `dto` packages.
- DTOs have not been introduced yet — controllers return raw JPA entities. When relationships grow more complex, introduce DTOs to decouple the API response shape from the persistence model.
