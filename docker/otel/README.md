# OpenTelemetry Java Agent

Download the agent to this folder (kept out of VCS):

```bash
curl -L -o docker/otel/opentelemetry-javaagent.jar \
  https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
```

Run any service with the agent, pointing to your OTLP collector (Jaeger/collector on localhost:4317 by default):

```bash
JAVA_TOOL_OPTIONS="-javaagent:docker/otel/opentelemetry-javaagent.jar" \
OTEL_SERVICE_NAME=catalog \
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
OTEL_TRACES_EXPORTER=otlp \
OTEL_METRICS_EXPORTER=none \
./mvnw -pl services/catalog spring-boot:run
```
