# API

Swagger UI is available at `/swagger-ui.html` when the backend is running.

Core endpoints:

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me`
- `GET /users`
- `POST /users`
- `PATCH /users/{id}`
- `PATCH /users/{id}/deactivate`
- `PATCH /users/{id}/reactivate`
- `POST /tickets`
- `GET /tickets`
- `GET /tickets/{id}`
- `PATCH /tickets/{id}`
- `DELETE /tickets/{id}`
- `POST /tickets/{id}/assign`
- `POST /tickets/{id}/status`
- `POST /tickets/{id}/comments`
- `GET /tickets/{id}/comments`
- `GET /tickets/{id}/audit-logs`
- `GET /dashboard/summary`
- `GET /dashboard/tickets-by-status`
- `GET /dashboard/tickets-by-priority`
- `GET /dashboard/tickets-by-assignee`

