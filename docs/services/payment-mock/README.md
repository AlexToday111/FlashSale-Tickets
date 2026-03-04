## Payment-mock service

- Consumes `payment.requested` events from `payments.exchange`.
- Simulates payment processing with random success/failure and delay.
- Publishes `payment.succeeded` / `payment.failed` events back to `payments.exchange`.
- Uses `processed_events` table to ensure idempotent handling of messages.

