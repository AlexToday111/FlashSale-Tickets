package com.ticketing.notification.metrics;

import com.ticketing.reservation.api.ReservationEventBindings;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RabbitBacklogMetrics {

    private static final Logger log = LoggerFactory.getLogger(RabbitBacklogMetrics.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String vhost;
    private final Map<String, AtomicInteger> queueDepths = new ConcurrentHashMap<>();

    public RabbitBacklogMetrics(RestTemplateBuilder builder,
                                MeterRegistry meterRegistry,
                                @Value("${rabbitmq.management.base-url}") String baseUrl,
                                @Value("${rabbitmq.management.username}") String username,
                                @Value("${rabbitmq.management.password}") String password,
                                @Value("${rabbitmq.management.vhost:/}") String vhost) {
        this.baseUrl = baseUrl;
        this.vhost = vhost;
        this.restTemplate = builder.basicAuthentication(username, password).build();

        registerGauge(meterRegistry, ReservationEventBindings.Q_NOTIFICATION_MAIN);
        registerGauge(meterRegistry, ReservationEventBindings.Q_NOTIFICATION_RETRY);
        registerGauge(meterRegistry, ReservationEventBindings.Q_NOTIFICATION_DLQ);
    }

    @Scheduled(fixedDelayString = "${rabbitmq.management.poll-interval:10000}")
    public void poll() {
        updateQueueDepth(ReservationEventBindings.Q_NOTIFICATION_MAIN);
        updateQueueDepth(ReservationEventBindings.Q_NOTIFICATION_RETRY);
        updateQueueDepth(ReservationEventBindings.Q_NOTIFICATION_DLQ);
    }

    private void registerGauge(MeterRegistry registry, String queueName) {
        AtomicInteger value = new AtomicInteger(0);
        queueDepths.put(queueName, value);
        Gauge.builder("queue_messages", value, AtomicInteger::get)
                .tag("queue", queueName)
                .register(registry);
    }

    private void updateQueueDepth(String queueName) {
        try {
            String url = baseUrl + "/api/queues/" + encode(vhost) + "/" + encode(queueName);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return;
            }
            Object messages = response.get("messages");
            if (messages instanceof Number n) {
                queueDepths.get(queueName).set(n.intValue());
            }
        } catch (Exception e) {
            log.debug("Failed to fetch queue depth for {}", queueName, e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
