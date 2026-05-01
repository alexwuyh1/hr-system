package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class InvalidParameterException extends BusinessException {
    public InvalidParameterException(String parameter, String message) {
        super("INVALID_PARAMETER", HttpStatus.BAD_REQUEST, 
            String.format("参数 %s 无效：%s", parameter, message));
    }
}
