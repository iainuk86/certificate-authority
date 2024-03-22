package net.majatech.ca.exceptions;

public class CaException extends RuntimeException {

    public CaException(String message) {
        super(message);
    }

    public CaException(String message, Exception e) {
        super(message, e);
    }
}
