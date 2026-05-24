create table users (
  id bigserial primary key,
  email text not null unique,
  password_hash text not null,
  full_name text not null,
  role text not null check (role in ('ADMIN', 'MANAGER', 'AGENT', 'REQUESTER')),
  active boolean not null default true,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create index idx_users_role on users(role);
create index idx_users_active on users(active);

