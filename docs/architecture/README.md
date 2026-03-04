## Architecture overview

- **Style**: modular monolith / small services (`catalog`, `inventory`, `order`, `payment-mock`, `notification`).
- **Sync APIs**: REST (Spring Boot, OpenAPI).
- **Async flows**:
  - reservation events: `reservation.events` → notification with retry/DLQ.
  - payment flow: `order` outbox → `payments.exchange` → `payment-mock` → payment result events → `order`.
- **Data stores**:
  - Postgres: один инстанс на сервис (`catalog`, `inventory`, `order`, `payment-mock`).
  - Redis: кэш для каталога.
- **Contracts**: общая библиотека `libs/contracts` (`EventEnvelope`, payment payloads).
- **Observability**: Prometheus, Grafana, OTEL agent, Jaeger.

