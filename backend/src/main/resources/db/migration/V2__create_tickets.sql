create table tickets (
  id bigserial primary key,
  title text not null,
  description text not null,
  status text not null check (status in ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED')),
  priority text not null check (priority in ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
  requester_id bigint not null references users(id),
  assignee_id bigint references users(id),
  due_at timestamptz,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  resolved_at timestamptz
);

create index idx_tickets_status on tickets(status);
create index idx_tickets_priority on tickets(priority);
create index idx_tickets_assignee on tickets(assignee_id);
create index idx_tickets_requester on tickets(requester_id);
create index idx_tickets_created_at on tickets(created_at);
create index idx_tickets_due_at on tickets(due_at);

