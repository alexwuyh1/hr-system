package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(String resourceType, String identifier) {
        super("CONFLICT", HttpStatus.CONFLICT,
            String.format("%s 已存在：%s", resourceType, identifier));
    }
}
