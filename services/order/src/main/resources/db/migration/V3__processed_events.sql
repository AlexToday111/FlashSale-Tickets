create table processed_events (
    event_id uuid primary key,
    processed_at timestamptz not null,
    consumer_name varchar(128) not null
);

