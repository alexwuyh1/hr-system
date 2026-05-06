package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    public NotFoundException(String resourceType, Long id) {
        super("NOT_FOUND", HttpStatus.NOT_FOUND,
            String.format("%s 不存在，ID: %d", resourceType, id));
    }

    public NotFoundException(String resourceType, String identifier) {
        super("NOT_FOUND", HttpStatus.NOT_FOUND,
            String.format("%s 不存在：%s", resourceType, identifier));
    }
}
