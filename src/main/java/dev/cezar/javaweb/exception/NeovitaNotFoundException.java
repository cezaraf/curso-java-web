package dev.cezar.javaweb.exception;

public class NeovitaNotFoundException extends NeovitaException {

    public NeovitaNotFoundException(String message) {
        super(message);
    }

    public NeovitaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
