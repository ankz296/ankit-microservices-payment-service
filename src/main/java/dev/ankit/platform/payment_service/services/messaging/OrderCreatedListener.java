package dev.ankit.platform.payment_service.services.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ankit.platform.payment_service.domain.Payment;
import dev.ankit.platform.payment_service.domain.PaymentStatus;
import dev.ankit.platform.payment_service.dto.OrderCreatedEvent;
import dev.ankit.platform.payment_service.dto.PaymentFailedEvent;
import dev.ankit.platform.payment_service.dto.PaymentProcessedEvent;
import dev.ankit.platform.payment_service.exception.InvalidEventException;
import dev.ankit.platform.payment_service.services.PaymentProcessor;
import dev.ankit.platform.payment_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
            OrderCreatedEvent event =
                    objectMapper.readValue(message, OrderCreatedEvent.class);

            if (event.getOrderId() == null || event.getUserId() == null) {
                throw new InvalidEventException("Missing required fields in order.created event");
            }

            String eventId = UUID.randomUUID().toString();

            log.info("Processing order.created event orderId={}, userId={}",
                    event.getOrderId(), event.getUserId());

            boolean success = paymentProcessor.isPaymentSuccessful();

            log.info("Payment decision orderId={}, success={}",
                    event.getOrderId(), success);

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

                log.info("Payment processed successfully orderId={}, paymentId={}",
                        event.getOrderId(), payment.getId());

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

                log.warn("Payment failed orderId={}, paymentId={}",
                        event.getOrderId(), payment.getId());
            }

        } catch (InvalidEventException e) {
            // ❌ BAD EVENT → skip (no retry)
            log.warn("Invalid event skipped payload={}, reason={}", message, e.getMessage());
        } catch (IllegalArgumentException e) {
            // ❌ Business issue → skip
            log.warn("Business validation failed payload={}, reason={}", message, e.getMessage());
        } catch (Exception e) {
            // ✅ SYSTEM FAILURE → retry + DLQ
            log.error("System failure processing order.created payload={}", message, e);

            throw new RuntimeException(e);
        }
    }
}