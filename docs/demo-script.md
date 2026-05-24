# Demo Script

Use the seed credentials from the README. All seed users use `password`.

1. Log in as `requester@example.com`.
2. Create a high-priority ticket from the Create Ticket page.
3. Open the ticket list and confirm the new ticket is visible.
4. Log out and log in as `manager@example.com`.
5. Open the ticket and assign it to `agent@example.com`.
6. Log out and log in as `agent@example.com`.
7. Move the ticket to `IN_PROGRESS`.
8. Add a comment.
9. Move the ticket to `RESOLVED`.
10. Log out and log in as `manager@example.com`.
11. Move the ticket to `CLOSED`.
12. Confirm the audit timeline includes creation, assignment, status changes, and comment events.
13. Confirm dashboard metrics update.
14. Log in as `requester@example.com` and confirm other requesters' tickets are not accessible.

## Verification Notes

- Default backend test command: `cd backend && mvn test`
- PostgreSQL integration tests: `cd backend && mvn test -DrunTestcontainers=true`
- Frontend build: `cd frontend && npm ci && npm run build`
- Compose syntax check: `docker compose config`
- Full system startup, when Docker daemon is healthy: `docker compose up --build`

