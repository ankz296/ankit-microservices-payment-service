package dev.ankit.platform.payment_service.services;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class PaymentProcessor {

    @Value("${payment.simulator.mode:ALWAYS_SUCCESS}")
    private String mode;

    @Value("${payment.simulator.success-rate:80}")
    private int successRate;

    /**
     * Real world: Bank / Payment Gateway decide karta hai
     * Tumhara system: PaymentSimulator decide karta hai
     * Why?
     * <p>
     * Failure injection
     * Chaos testing
     * Saga + compensation validate karne ke liye
     *
     * @return
     */
    public boolean isPaymentSuccessful() {

        if ("ALWAYS_SUCCESS".equalsIgnoreCase(mode)) {
            log.debug("Payment simulation mode=ALWAYS_SUCCESS");
            return true;
        }

        if ("ALWAYS_FAIL".equalsIgnoreCase(mode)) {
            log.debug("Payment simulation mode=ALWAYS_FAIL");
            return false;
        }

        int roll = ThreadLocalRandom.current().nextInt(1, 101);

        boolean result = roll <= successRate;

        log.debug("Payment simulation RANDOM roll={}, successRate={}, result={}",
                roll, successRate, result);

        return result;
    }
}
