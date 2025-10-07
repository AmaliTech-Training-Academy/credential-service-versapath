package com.capstone.exception;

public class BadgeProcessingException extends RuntimeException {
    public BadgeProcessingException(String message) {
        super(message);
    }
    public BadgeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
