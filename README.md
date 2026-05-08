# Event Ticket SaaS — Backend

Multi-tenant platform for selling event tickets. Organizers manage events and ticket types; attendees purchase tickets via Stripe.

## Tech Stack
- Java 21 (LTS), Spring Boot 3.3
- PostgreSQL 16
- Docker / Docker Compose
- Flyway (migrations)

## Quick Start

  ### Prerequisites
  - Java 21
  - Docker Desktop
  - Git

## Setup

     1. Clone the repo:
```bash
        git clone https://github.com/YOUR-USERNAME/event-ticket-saas.git
        cd event-ticket-saas
```

     2. Start PostgreSQL:
```bash
        docker-compose up -d
```

     3. Run the app:
```bash
        mvn spring-boot:run
```

     4. Test:
```bash
        curl http://localhost:8080/hello
        curl http://localhost:8080/users
```

**Project Structure**
- `User.java` — Entity with JPA annotations
- `UserRepository.java` — JPA interface for CRUD operations
- `UserController.java` — REST API endpoints (/users GET/POST)
- `HelloController.java` — Test endpoint
- `SecurityConfig.java` — Spring Security configuration
- `application.properties` — Database connection settings

## Next Steps
     - Week 2: JWT authentication (register, login)
     - Week 3: Multi-tenancy (Organization model)