.PHONY: infra-up infra-down run-services demo-flow load-test

infra-up:
	cd docker && docker compose up -d

infra-down:
	cd docker && docker compose down -v

run-services:
	@echo "Run services in separate terminals:"
	@echo "mvn -pl services/catalog spring-boot:run"
	@echo "mvn -pl services/inventory spring-boot:run"
	@echo "mvn -pl services/order spring-boot:run"
	@echo "mvn -pl services/payment-mock spring-boot:run"
	@echo "mvn -pl services/notification spring-boot:run"

demo-flow:
	@echo "1) Create reservation"
	curl -s -X POST http://localhost:8082/reservation \
	  -H 'Content-Type: application/json' \
	  -H 'X-Correlation-Id: demo-flow-1' \
	  -d '{"userId":"11111111-1111-1111-1111-111111111111","eventId":"00000000-0000-0000-0000-000000000002","seats":["00000000-0000-0000-0000-000000000003"]}'
	@echo "\n2) Create order"
	@echo "curl -X POST http://localhost:8083/orders -H 'Content-Type: application/json' -H 'X-Correlation-Id: demo-flow-1' -d '{\"reservationId\":\"<reservation_id>\"}'"
	@echo "3) Inspect trace in Jaeger: service=order, operation=POST /orders"

load-test:
	k6 run scripts/load/reservations.js
