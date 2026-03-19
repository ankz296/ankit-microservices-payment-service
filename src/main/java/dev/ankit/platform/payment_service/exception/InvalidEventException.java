package dev.ankit.platform.payment_service.exception;

public class InvalidEventException extends RuntimeException {
    public InvalidEventException(String message) {
        super(message);
    }
}
