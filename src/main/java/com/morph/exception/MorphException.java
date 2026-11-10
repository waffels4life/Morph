package com.morph.exception;

public class MorphException extends RuntimeException {

    public MorphException(String message) {
        super(message);
    }

    public MorphException(String message, Throwable cause) {
        super(message, cause);
    }
}
