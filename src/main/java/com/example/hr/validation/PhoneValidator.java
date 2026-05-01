package com.example.hr.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // 可选字段
        }
        
        boolean matches = Pattern.matches(PHONE_PATTERN, phone);
        
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "手机号格式不正确，应为 11 位中国大陆手机号（如：13800138000）")
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
