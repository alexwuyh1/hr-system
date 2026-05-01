package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceType, String identifier) {
        super("DUPLICATE_RESOURCE", HttpStatus.CONFLICT, 
            String.format("%s 已存在：%s", resourceType, identifier));
    }
}
