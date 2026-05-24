# Security

Authentication uses JWT bearer tokens signed with `JWT_SECRET`.

Authorization is enforced server-side in services and controllers. The frontend hides unauthorized controls only as a usability layer.

Role summary:

- ADMIN: user management and all tickets
- MANAGER: all tickets, assignment, priority, closing, reopening
- AGENT: assigned tickets
- REQUESTER: own tickets

