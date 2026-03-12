package dev.ankit.platform.payment_service.services.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ankit.platform.payment_service.domain.Payment;
import dev.ankit.platform.payment_service.domain.PaymentStatus;
import dev.ankit.platform.payment_service.dto.OrderCreatedEvent;
import dev.ankit.platform.payment_service.dto.PaymentFailedEvent;
import dev.ankit.platform.payment_service.dto.PaymentProcessedEvent;
import dev.ankit.platform.payment_service.services.PaymentProcessor;
import dev.ankit.platform.payment_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final ObjectMapper objectMapper;
    private final PaymentProcessor paymentProcessor;
    private final PaymentService paymentService;
    private final PaymentEventPublisher publisher;

    @KafkaListener(topics = "order.created", groupId = "payment-service-group")
    public void onMessage(String message) {
        try {
            String eventId = java.util.UUID.randomUUID().toString();
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("📥 Received order.created event orderId={}", event.getOrderId());

            boolean success = paymentProcessor.isPaymentSuccessful();

            Payment payment = paymentService.createPayment(
                    event.getOrderId(),
                    event.getUserId(),
                    event.getTotalAmount(),
                    success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED
            );

            long now = System.currentTimeMillis();

            if (success) {
                PaymentProcessedEvent processed = PaymentProcessedEvent.builder()
                        .eventId(eventId)
                        .orderId(event.getOrderId())
                        .paymentId(payment.getId())
                        .userId(event.getUserId())
                        .amount(event.getTotalAmount())
                        .status("SUCCESS")
                        .eventTime(now)
                        .build();

                publisher.publishProcessed(processed);

            } else {
                PaymentFailedEvent failed = PaymentFailedEvent.builder()
                        .eventId(eventId)
                        .orderId(event.getOrderId())
                        .paymentId(payment.getId())
                        .userId(event.getUserId())
                        .amount(event.getTotalAmount())
                        .status("FAILED")
                        .reason("PAYMENT_DECLINED")
                        .eventTime(now)
                        .build();

                publisher.publishFailed(failed);
            }

        } catch (Exception e) {
            log.error("❌ Failed to process order.created message={}", message, e);
            // Later: DLQ / retry strategy
        }
    }
}