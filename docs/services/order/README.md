## Order service

- Creates orders based on reservations from `inventory`.
- Uses outbox table to emit payment-related events via RabbitMQ (`payments.exchange`).
- Consumes payment results (`payment.succeeded` / `payment.failed`) and updates order status idempotently.
- Stores processed event IDs in `processed_events` to avoid double-processing.

