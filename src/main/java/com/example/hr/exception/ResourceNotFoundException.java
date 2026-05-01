package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceType, Long id) {
        super("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, 
            String.format("%s 不存在，ID: %d", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, 
            String.format("%s 不存在：%s", resourceType, identifier));
    }
}
