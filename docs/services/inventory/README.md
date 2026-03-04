## Inventory service

- Reservation lifecycle: create, cancel, expire.
- Stores reservations and seat state in Postgres.
- Periodic scheduler to expire reservations and release seats.
- Exposes OpenAPI UI at `/swagger-ui.html` and metrics at `/actuator/prometheus`.

