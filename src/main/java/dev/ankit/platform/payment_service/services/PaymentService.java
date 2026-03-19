package dev.ankit.platform.payment_service.services;

import dev.ankit.platform.payment_service.domain.Payment;
import dev.ankit.platform.payment_service.domain.PaymentStatus;
import dev.ankit.platform.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(UUID orderId, UUID userId, BigDecimal amount, PaymentStatus status) {

        log.info("Creating payment orderId={}, userId={}, amount={}, status={}",
                orderId, userId, amount, status);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .status(status)
                .build();

        Payment saved = paymentRepository.save(payment);

        log.info("Payment persisted paymentId={}, orderId={}, status={}",
                saved.getId(), orderId, status);

        return saved;
    }
}