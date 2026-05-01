package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateException extends BusinessException {
    public InvalidStateException(String message) {
        super("INVALID_STATE", HttpStatus.BAD_REQUEST, message);
    }

    public InvalidStateException(String resourceType, String currentState, String expectedState) {
        super("INVALID_STATE", HttpStatus.BAD_REQUEST, 
            String.format("%s 状态错误：当前状态 %s，期望状态 %s", resourceType, currentState, expectedState));
    }
}
