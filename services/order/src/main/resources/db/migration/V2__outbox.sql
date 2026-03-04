create type outbox_status as enum ('NEW', 'SENT', 'FAILED');

create table outbox (
    id uuid primary key,
    aggregate_type varchar(64) not null,
    aggregate_id uuid not null,
    event_type varchar(128) not null,
    payload jsonb not null,
    status outbox_status not null,
    created_at timestamptz not null,
    sent_at timestamptz,
    attempts integer not null default 0,
    last_error text
);

create index idx_outbox_status_created on outbox(status, created_at);

