create table seat_state (
    seat_id uuid primary key,
    status varchar(32) not null,
    version bigint,
    updated_at timestamptz not null
);

create index idx_seat_status on seat_state(status);
create index idx_seat_updated on seat_state(updated_at);

create table reservation (
    reservation_id uuid primary key,
    user_id uuid not null,
    event_id uuid not null,
    status varchar(32) not null,
    expires_at timestamptz not null
);

create index idx_reservation_user on reservation(user_id);
create index idx_reservation_event on reservation(event_id);
create index idx_reservation_status on reservation(status);

create table reservation_seats (
    reservation_id uuid not null references reservation(reservation_id) on delete cascade,
    seat_id uuid not null,
    primary key (reservation_id, seat_id)
);

