## Infrastructure & observability

- **Docker compose**: `docker/compose.yml`
  - Postgres (per service)
  - RabbitMQ (5672/15672)
  - Prometheus (9090)
  - Grafana (3000)
  - Jaeger (16686, OTLP 4317)
  - Redis (catalog cache)
- **Metrics**:
  - each service exposes `/actuator/prometheus`
  - Prometheus targets configured in `docker/prometheus/prometheus.yml`
- **Grafana**:
  - datasource provisioning: `docker/grafana/provisioning/datasource/datasource.yml`
  - default login: `admin/admin`
- **Tracing**:
  - OTEL Java agent instructions: `docker/otel/README.md`

