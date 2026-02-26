# FlashSale Tickets (monorepo)
## Модули
- `services/catalog` — каталог событий/билетов.
- `services/inventory` — остатки и резервы.
- `services/order` — оформление заказов.
- `services/payment-mock` — заглушка платежей.
- `services/notification` — уведомления.

## Инфраструктура
- Поднять всю обвязку: `cd docker && docker compose up -d`
- Включает: Postgres (3 инстанса), RabbitMQ (5672/15672), Jaeger (16686, OTLP 4317/4318), Prometheus (9090), Grafana (3000).
- Prometheus смотрит на локально запущенные сервисы через `host.docker.internal:8081/8082/8083` (см. `docker/prometheus/prometheus.yml`).
- Grafана уже с провиженингом Prometheus datasource (см. `docker/grafana/provisioning/datasource/datasource.yml`), логин `admin/admin` по умолчанию.

## Сборка и запуск
- Сборка всего: `mvn clean package`
- Запуск отдельного сервиса: `mvn -pl services/<svc> spring-boot:run`  
  (<svc> = catalog | inventory | order | payment-mock | notification)
- Все сервисы слушают `8080` по умолчанию. При одновременном запуске меняй порт флагом `--server.port=8081` (или правь `src/main/resources/application.yml`).
- Actuator: `/actuator/health`, `/actuator/info`, `/actuator/prometheus`
- OpenAPI UI: `/swagger-ui.html`

### Пример запуска трёх сервисов локально (совместимо с Prometheus targets)
- Каталог: `mvn -pl services/catalog spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`
- Инвентори: `mvn -pl services/inventory spring-boot:run -Dspring-boot.run.arguments=--server.port=8082`
- Ордер: `mvn -pl services/order spring-boot:run -Dspring-boot.run.arguments=--server.port=8083`

## Конфигурация по умолчанию
В каждом сервисе `src/main/resources/application.yml` содержит:
- `server.port: 8080`
- health/info/prometheus включены
- тег метрик `application=${spring.application.name}`
- `spring.application.name` выставлен под имя сервиса

## Наблюдаемость (Day 1)
Открой `docker/otel/README.md` — там команда для скачивания `opentelemetry-javaagent.jar` и пример запуска:
```
JAVA_TOOL_OPTIONS="-javaagent:docker/otel/opentelemetry-javaagent.jar" \
OTEL_SERVICE_NAME=catalog \
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
OTEL_TRACES_EXPORTER=otlp \
OTEL_METRICS_EXPORTER=none \
mvn -pl services/catalog spring-boot:run
```
Метрики идут в Prometheus (`/actuator/prometheus`), трейсинг — через OTLP.

## Быстрые проверки (curl)
- Health: `curl -f http://localhost:8081/actuator/health` (замени порт на свой)
- Prometheus metrics: `curl http://localhost:8081/actuator/prometheus | head -n 5`
- OpenAPI redirect: `curl -I http://localhost:8081/swagger-ui.html`

## Зависимости
- Управление версиями в корневом `pom.xml` (BOM Spring Boot, springdoc 2.6.0).
- Общие зависимости сервисов: web, validation, actuator, micrometer-prometheus, springdoc, tests.

## Где смотреть
- Prometheus targets: `http://localhost:9090/targets`
- Grafana datasource/dashboards: `http://localhost:3000` (логин `admin/admin`)
- Jaeger UI (трейсы): `http://localhost:16686`

## Следующие шаги
1. Скачай OTEL агент и запусти сервисы с портами 8081/8082/8083 под OTLP endpoint `http://localhost:4317`.
2. Убедись, что цели в Prometheus зеленые; открой Grafana и Jaeger для проверки метрик/трейсов.
3. При необходимости добавляй JPA/брокер зависимости в сервисные `pom.xml`.
