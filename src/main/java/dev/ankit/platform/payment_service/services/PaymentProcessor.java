package dev.ankit.platform.payment_service.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaymentProcessor {

    @Value("${payment.simulator.mode:ALWAYS_SUCCESS}")
    private String mode;

    @Value("${payment.simulator.success-rate:80}")
    private int successRate;

    public boolean isPaymentSuccessful() {
        if ("ALWAYS_SUCCESS".equalsIgnoreCase(mode)) return true;
        if ("ALWAYS_FAIL".equalsIgnoreCase(mode)) return false;

        // RANDOM
        int roll = ThreadLocalRandom.current().nextInt(1, 101);
        return roll <= successRate;
    }
}
