# Enterprise Ticket Management System

A full-stack internal service-ticket platform for creating, assigning, tracking, commenting on, and auditing enterprise support tickets.

## Tech Stack

- Backend: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Flyway, JWT, PostgreSQL
- Frontend: React, TypeScript, Vite, Material UI, React Router, Axios
- Testing: JUnit 5, Mockito, Spring Security Test, Testcontainers
- DevOps: Docker Compose, Swagger/OpenAPI

## Local Seed Users

All demo users use the password `password`.

| Email | Role |
|---|---|
| `admin@example.com` | ADMIN |
| `manager@example.com` | MANAGER |
| `agent@example.com` | AGENT |
| `requester@example.com` | REQUESTER |

## Quick Start

```bash
cp .env.example .env
docker compose up --build
```

Services:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## Run Locally Without Docker

Start PostgreSQL, then:

```bash
cd backend
mvn spring-boot:run
```

In another shell:

```bash
cd frontend
npm install
npm run dev
```

## Tests

```bash
cd backend
mvn test
```

Run PostgreSQL-backed controller/security tests with Docker:

```bash
cd backend
mvn test -DrunTestcontainers=true
```

```bash
cd frontend
npm install
npm run build
```

## Demo Workflow

1. Log in as `requester@example.com` and create a ticket.
2. Log in as `manager@example.com` and assign it to `agent@example.com`.
3. Log in as `agent@example.com`, move it to `IN_PROGRESS`, add a comment, then resolve it.
4. Log in as `manager@example.com` and close it.
5. Review the ticket audit timeline and dashboard metrics.
6. Log in as a requester and confirm unauthorized ticket access is rejected.

## Role Permissions

| Role | Permissions |
|---|---|
| ADMIN | Manage users, view all tickets, deactivate/reactivate users, change roles |
| MANAGER | View all tickets, assign tickets, change priority, reopen and close tickets |
| AGENT | View and update assigned tickets, add comments to assigned tickets |
| REQUESTER | Create tickets, view own tickets, add comments to own tickets |

## Known Limitations

- Email notifications and file attachments are intentionally out of scope for v1.
- Seed data is for local demos only and can be disabled with `APP_SEED_ENABLED=false`.
- Override `JWT_SECRET` outside local demos; the committed default is only for local development.
- The frontend uses host-local backend URLs by default for Docker demos.

## License

MIT. See [LICENSE](LICENSE).
