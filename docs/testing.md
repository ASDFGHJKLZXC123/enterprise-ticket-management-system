# Testing

Backend tests cover:

- ticket creation
- assignment rules
- status workflow
- dashboard counts
- audit logging
- PostgreSQL-backed controller/security behavior with Testcontainers

Default local command:

```bash
cd backend
mvn test
```

PostgreSQL-backed integration command:

```bash
cd backend
mvn test -DrunTestcontainers=true
```

Frontend quality gates:

- TypeScript build
- manual role workflow across requester, manager, agent, and admin
