# Architecture

The application is split into a Spring Boot API, PostgreSQL database, and React frontend.

```text
Browser -> React/MUI frontend -> Spring Boot REST API -> PostgreSQL
                                      |
                                      +-> Flyway migrations
                                      +-> JWT authentication
                                      +-> RBAC service checks
```

Backend package root: `com.example.tickets`.

## Backend Layers

- Controllers expose REST endpoints and validate request DTOs.
- Services enforce workflow rules, role permissions, and audit logging.
- Repositories use Spring Data JPA and specifications for filtering.
- Flyway owns the schema.

## Frontend Layers

- API clients centralize Axios calls and JWT attachment.
- Pages own route-level workflows.
- Components render reusable tables, chips, timelines, forms, and cards.

