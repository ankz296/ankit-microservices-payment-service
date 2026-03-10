package dev.ankit.platform.payment_service.services;

import dev.ankit.platform.payment_service.domain.Payment;
import dev.ankit.platform.payment_service.domain.PaymentStatus;
import dev.ankit.platform.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(UUID orderId, UUID userId, BigDecimal amount, PaymentStatus status) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .status(status)
                .build();
        return paymentRepository.save(payment);
    }
}