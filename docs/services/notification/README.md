## Notification service

- Consumes reservation events from `reservation.events` exchange.
- Uses retry + DLQ queues to handle transient failures.
- Exposes metrics for processed/failures/DLQ messages.
- Useful for demonstrating robust event consumption patterns.

