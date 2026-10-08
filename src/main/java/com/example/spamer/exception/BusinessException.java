package com.example.spamer.exception;

// Domain rule broken (e.g. not enough balance to send). Maps to 422.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
