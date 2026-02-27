-- events
CREATE TABLE IF NOT EXISTS event (
    id uuid PRIMARY KEY,
    title text NOT NULL,
    venue text NOT NULL,
    starts_at timestamptz NOT NULL,
    status text NOT NULL DEFAULT 'PUBLISHED',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE index IF NOT EXISTS idx_event_starts_at ON event(starts_at);
CREATE index IF NOT EXISTS idx_event_status ON event(status);

-- seat
CREATE TABLE IF NOT EXISTS seat (
    id uuid PRIMARY KEY,
    event_id uuid NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    section text NOT NULL,
    row_label text NOT NULL,
    seat_number text NOT NULL,
    price_cents bigint NOT NULL CHECK (price_cents >= 0),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE index IF NOT EXISTS ux_seat_event_place
    ON seat(event_id, section, row_label, seat_number);

CREATE index IF NOT EXISTS idx_seat_event_id ON seat(event_id);
