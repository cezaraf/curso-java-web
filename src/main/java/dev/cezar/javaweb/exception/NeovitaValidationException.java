package dev.cezar.javaweb.exception;

public class NeovitaValidationException extends RuntimeException {

    public NeovitaValidationException(String message) {
        super(message);
    }

    public NeovitaValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
