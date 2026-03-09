# FlashSale Tickets

Интервью-ready MVP для flash-sale бронирования билетов: синхронный API, асинхронные события, наблюдаемость (metrics + traces + logs), демо-сценарии и нагрузочный smoke test.

## Архитектура

```
flowchart LR
    Client --> Gateway[API entrypoint\n(Order API)]
    Gateway --> Order
    Order --> Inventory
    Order --> Rabbit[(RabbitMQ)]
    Rabbit --> Payment
    Payment --> Rabbit
    Rabbit --> Order
    Rabbit --> Notification

    Catalog[(Catalog DB)]
    Inventory[(Inventory DB)]
    Orders[(Order DB)]

    CatalogSvc[Catalog Service] --> Catalog
    Inventory --> InventoryDB[(Postgres Inventory)]
    Order --> Orders
```

Сервисы:
- `catalog` (8081): события и места.
- `inventory` (8082): резервации, отмена, экспирация.
- `order` (8083): создание заказа + saga orchestration.
- `payment-mock` (8084): обработка `PaymentRequested` и публикация результата.
- `notification` (8085): consumer reservation-событий с retry + DLQ.

Инфраструктура (`docker/compose.yml`): Postgres x3, RabbitMQ, Prometheus, Grafana, Jaeger, Redis.

## Быстрый старт

```bash
make infra-up
```

Запуск сервисов (локально, в отдельных терминалах):
```bash
mvn -pl services/catalog spring-boot:run
mvn -pl services/inventory spring-boot:run
mvn -pl services/order spring-boot:run
mvn -pl services/payment-mock spring-boot:run
mvn -pl services/notification spring-boot:run
```

## Наблюдаемость

### Grafana
- URL: `http://localhost:3000` (`admin/admin`)
- Дашборд: **FlashSale Observability** (provisioned)
- Панели:
  - latency p95 по сервисам
  - errors (5xx)
  - RPS
  - consumer failures + DLQ size
  - бизнес-метрики: `reserved_count`, `sold_count`, `payment_fail_rate`

### Prometheus
- URL: `http://localhost:9090`
- Scrape targets: catalog/inventory/order/payment/notification + prometheus.

### Tracing
- URL: `http://localhost:16686`
- Поток для демо:
  `Gateway(Order API) → Order → Inventory → RabbitMQ → Payment → RabbitMQ → Order`
- Для связности запросов используйте заголовок `X-Correlation-Id`.

## Demo-сценарий

### 1) Создать резервацию
```bash
curl -s -X POST http://localhost:8082/reservation \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: demo-flow-1' \
  -d '{"userId":"11111111-1111-1111-1111-111111111111","eventId":"00000000-0000-0000-0000-000000000002","seats":["00000000-0000-0000-0000-000000000003"]}'
```

### 2) Создать заказ (подставить `reservationId`)
```bash
curl -s -X POST http://localhost:8083/orders \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: demo-flow-1' \
  -d '{"reservationId":"<reservationId>"}'
```

### 3) Проверить заказ
```bash
curl -s http://localhost:8083/orders/<orderId>
```

### 4) Проверить traces/graphs
- Jaeger: найти трейс по service `order`.
- Grafana: открыть `FlashSale Observability`.

## Нагрузочный мини-тест (100 параллельных броней)

```bash
make load-test
```

Или напрямую:
```bash
k6 run scripts/load/reservations.js
```

## OpenAPI

Swagger UI:
- Catalog: `http://localhost:8081/swagger-ui.html`
- Inventory: `http://localhost:8082/swagger-ui.html`
- Order: `http://localhost:8083/swagger-ui.html`
- Payment-mock: `http://localhost:8084/swagger-ui.html`
- Notification: `http://localhost:8085/swagger-ui.html`
