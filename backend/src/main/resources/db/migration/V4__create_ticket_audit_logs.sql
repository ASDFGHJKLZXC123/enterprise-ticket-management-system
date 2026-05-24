create table ticket_audit_logs (
  id bigserial primary key,
  ticket_id bigint not null references tickets(id) on delete cascade,
  actor_id bigint references users(id),
  action text not null,
  old_value text,
  new_value text,
  created_at timestamptz not null
);

create index idx_ticket_audit_logs_ticket on ticket_audit_logs(ticket_id);
create index idx_ticket_audit_logs_created_at on ticket_audit_logs(created_at);

