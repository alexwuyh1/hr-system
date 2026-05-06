package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", HttpStatus.BAD_REQUEST, message);
    }

    public BadRequestException(String resourceType, String currentState, String expectedState) {
        super("BAD_REQUEST", HttpStatus.BAD_REQUEST,
            String.format("%s 状态错误：当前状态 %s，期望状态 %s", resourceType, currentState, expectedState));
    }
}
