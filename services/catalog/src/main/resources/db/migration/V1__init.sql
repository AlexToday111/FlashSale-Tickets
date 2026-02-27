-- events
CREATE TABLE IF NOT EXISTS event (
    id uuid PRIMARY KEY,
    title text NOT NULL,
    venue text NOT NULL,
    starts_at timestampz NOT NULL,
    status text NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMPZ not null default now(),
    updated_at TIMESTAMPZ not null default now()
);

CREATE index IF NOT EXISTS idx_event_starts_at ON event(starts_at);
CREATE index IF NOT EXISTS idx_event_status ON event(status);

-- seat
CREATE TABLE IF NOT EXISTS seat(
    id uuid PRIMARY KEY,
    event_id uuid NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    SECTION text NOT NULL,
    row_label text NOT NULL,
    seat_number text NOT NULL,
    price numeric(12, 2) NOT NULL CHECK(price >= 0),
    created_at TIMESTAMPZ NOT NULL DEFAULT now()
);

CREATE UNIQUE index IF NOT EXISTS ux_seat_event_place
    ON seat(event_id, SECTION, row_label, seat_number);

CREATE index IF NOT EXISTS idx_seat_event_id ONT seat(event_id);