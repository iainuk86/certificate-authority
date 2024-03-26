package net.majatech.ca.exceptions;

/**
 * Exception used to wrap any other exception that occurs during the running of the application
 */
public class CaException extends RuntimeException {

    public CaException(String message) {
        super(message);
    }

    public CaException(String message, Exception e) {
        super(message, e);
    }
}
