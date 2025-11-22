package com.Shakwa.utils.exception;

public class LockedException extends RuntimeException {
    public LockedException(String message) {
        super(message);
    }
    
    public LockedException(String message, Throwable cause) {
        super(message, cause);
    }
}

