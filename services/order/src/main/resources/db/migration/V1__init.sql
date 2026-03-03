create table orders (
    order_id uuid primary key,
    user_id uuid not null,
    reservation_id uuid not null,
    status varchar(32) not null,
    total integer not null,
    created_at timestamptz not null,
    constraint uk_orders_reservation unique (reservation_id)
);

create index idx_orders_user_created on orders(user_id, created_at);
create index idx_orders_status_created on orders(status, created_at);

