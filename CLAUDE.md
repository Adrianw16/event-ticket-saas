# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Roadmap

Full roadmap: `C:\Users\luque\Documents\Claude\Projects\Life Project\platform_engineering_roadmap.md`

**4 phases, ~1,190 hours, April 2026 → May 2027. Target: Junior Backend Developer → Platform Engineering.**

| Phase | Project | Stack | Deadline |
|-------|---------|-------|----------|
| 1 | Event Ticket SaaS Backend | Java 21 / Spring Boot 3 / PostgreSQL | Sep 13, 2026 |
| 2 | Payment + Notification Microservice | Go / RabbitMQ | Oct 31, 2026 |
| 3 | Event Analytics Pipeline | Go / TimescaleDB | Jan 31, 2027 |
| 4 | Multi-Tenant Observability Platform | Go / Prometheus / Terraform | May 31, 2027 |

**Phase 1 week-by-week summary:**

| Week | Dates | Focus | Key Deliverable |
|------|-------|-------|-----------------|
| 1 | May 4–10 | Environment + Spring Boot | Running app + PostgreSQL + first migration |
| 2 | May 11–17 | JWT Authentication | Register + login endpoints with JWT |
| 3 | May 18–24 | Multi-tenancy + Org Model | Tenant isolation, all queries scoped to org_id |
| 4 | May 25–31 | Event CRUD | Full event CRUD with publish/unpublish, S3 images |
| 5 | Jun 1–7 | Ticket Types + Inventory | Optimistic locking via @Version, oversell prevention |
| 6 | Jun 8–14 | Stripe Connect | Organizer Stripe account connected |
| 7 | Jun 15–21 | Order + Checkout | End-to-end checkout flow |
| 8 | Jun 22–28 | Email + AWS Staging | SendGrid confirmation email, first staging deploy |
| 9 | Jun 29–Jul 5 | Attendee + QR Check-in | Attendee list, CSV export, QR token check-in |
| 10 | Jul 6–12 | Dashboard APIs | Analytics endpoints + SSE real-time updates |
| 11 | Jul 13–19 | Redis Caching | Browse endpoint cached, TTL + event-driven invalidation |
| 12 | Jul 20–26 | DB Performance | 3 slowest queries optimized with EXPLAIN ANALYZE |
| 13 | Jul 27–Aug 2 | Subscriptions + Admin | Tier enforcement, admin endpoints |
| 14 | Aug 3–9 | Security + Rate Limiting | Bucket4j rate limits, OWASP headers |
| 15 | Aug 10–16 | Testing Sprint | 80%+ unit coverage, integration tests, load test |
| 16 | Aug 17–23 | OpenAPI Docs | Swagger UI live, ARCHITECTURE.md |
| 17 | Aug 24–30 | Flyway CI/CD | Flyway as mandatory pre-deploy step in GitHub Actions |
| 18 | Aug 31–Sep 6 | AWS Production | GitHub Actions pipeline, prod URL live, CloudWatch |
| 19 | Sep 7–13 | Final Polish | All success criteria checked, Phase 1 complete |

When the user says "I'm on Phase X, Week Y, Day Z" — read the corresponding day row in the roadmap file for the exact task, session type (LEARN/BUILD/TEST/REVIEW/INFRA/CHECKPOINT), and milestone.

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

Spring Boot 3.5 + Java 21 REST API backed by PostgreSQL (via Docker on port 5433).

Package structure: `com.eventticket.{config, controller, model, repository, service, transferobject}`

**Request flow**: HTTP request → `@RestController` → `@Service` → `JpaRepository` → PostgreSQL

**Key constraints**:
- `spring.jpa.hibernate.ddl-auto=none` — Hibernate never touches the schema. All schema changes go through Flyway migrations in `src/main/resources/db/migration/` using the `V{n}__{description}.sql` naming convention.
- Spring Security is present but fully open (`anyRequest().permitAll()`, CSRF disabled) — authentication is not yet implemented.
- Entities use Lombok `@Data`. Jackson serialization of lazy-loaded JPA relations will fail — use `@JsonIgnore` on `@ManyToOne(fetch = LAZY)` fields, or return DTOs instead of raw entities.

## Data Model

- `users` — `id`, `email`, `password_hash`, `role` (ORGANIZER/ADMIN), `created_at`, `org_id` (FK → organizations)
- `organizations` — `id`, `name`, `slug`, `email`, `created_at`
- A user belongs to one organization; org side has no back-reference yet.

## Database

- Host: `localhost:5433` (Docker-mapped)
- Database: `event_ticket_db`
- Credentials in `src/main/resources/application.properties` (not in `.env`)

## What Has Been Built

### Completed (Phase 1, Weeks 1–2)
- Spring Boot scaffold with Web, JPA, PostgreSQL, Security, Validation, Flyway, Actuator
- `GET /hello` — smoke test endpoint
- `User` entity + `UserRepository` + `UserController` (`GET /users`, `POST /users`)
- `Organization` entity + `OrganizationRepository` + `OrganizationController` (`GET /organizations`, `POST /organizations`)
- Flyway V1: creates `users` table
- Flyway V2: creates `organizations` table, adds `org_id NOT NULL FK` to `users`
- Flyway V3: adds `role VARCHAR(50) NOT NULL DEFAULT 'ORGANIZER'` to `users`
- `JwtService` — generate, validate, extract claims (jjwt 0.12, HMAC-SHA384)
- `AuthService` + `AuthController` — `POST /api/v1/auth/register` (201 + JWT) and `POST /api/v1/auth/login` (200 + JWT)
- `SecurityConfig`: CSRF disabled, all endpoints permit all (JWT filter not yet wired — Week 2 Thursday)

### Known Design Decisions
- `User.organization` is `@ManyToOne(fetch = LAZY)` with `@JsonIgnore` — excluded from `/users` responses to avoid Hibernate proxy serialization errors.
- `AuthRequest` requires `organizationName` — register always creates an org + owner user atomically.
- JWT claims: `sub` (userId), `org_id`, `email`, `iat`, `exp`. Default expiry 24h (`jwt.expiration-ms`, overridable via `application.properties`).
- `SecurityConfig` is still fully open — `JwtAuthenticationFilter` (Week 2 Thursday) will lock down `/api/v1/**`.
