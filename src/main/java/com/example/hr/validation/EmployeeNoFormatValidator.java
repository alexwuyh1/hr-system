package com.example.hr.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmployeeNoFormatValidator implements ConstraintValidator<EmployeeNoFormat, String> {
    
    private String pattern;

    @Override
    public void initialize(EmployeeNoFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String employeeNo, ConstraintValidatorContext context) {
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            return true; // 由@NotBlank 处理
        }
        
        boolean matches = Pattern.matches(pattern, employeeNo);
        
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "员工工号格式不正确，应为：2-3 个大写字母 + 4-6 位数字（如：EMP123456）")
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
