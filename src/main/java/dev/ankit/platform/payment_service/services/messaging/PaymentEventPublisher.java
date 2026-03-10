package dev.ankit.platform.payment_service.services.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ankit.platform.payment_service.dto.PaymentFailedEvent;
import dev.ankit.platform.payment_service.dto.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private static final String TOPIC_PAYMENT_PROCESSED = "payment.processed";
    private static final String TOPIC_PAYMENT_FAILED = "payment.failed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishProcessed(PaymentProcessedEvent event) {
        send(TOPIC_PAYMENT_PROCESSED, event.getOrderId().toString(), toJson(event));
    }

    public void publishFailed(PaymentFailedEvent event) {
        send(TOPIC_PAYMENT_FAILED, event.getOrderId().toString(), toJson(event));
    }

    private void send(String topic, String key, String payload) {
        kafkaTemplate.send(topic, key, payload);
        log.info("📤 Published to topic={} key={}", topic, key);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}