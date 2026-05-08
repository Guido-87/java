package com.spring.ia.exception;

public class GroqClientException extends RuntimeException {
    public GroqClientException(String message) {
        super(message);
    }

    public GroqClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
