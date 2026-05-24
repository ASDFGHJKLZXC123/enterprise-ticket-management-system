create table ticket_comments (
  id bigserial primary key,
  ticket_id bigint not null references tickets(id) on delete cascade,
  author_id bigint not null references users(id),
  body text not null,
  created_at timestamptz not null
);

create index idx_ticket_comments_ticket on ticket_comments(ticket_id);
create index idx_ticket_comments_created_at on ticket_comments(created_at);

