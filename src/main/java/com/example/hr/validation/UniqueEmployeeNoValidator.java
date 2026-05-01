package com.example.hr.validation;

import com.example.hr.repository.EmployeeRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmployeeNoValidator implements ConstraintValidator<UniqueEmployeeNo, String> {
    
    private final EmployeeRepository employeeRepository;

    public UniqueEmployeeNoValidator(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public boolean isValid(String employeeNo, ConstraintValidatorContext context) {
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            return true; // 由@NotBlank 处理
        }
        
        boolean exists = employeeRepository.findByEmployeeNo(employeeNo).isPresent();
        
        if (exists) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("员工工号 '" + employeeNo + "' 已存在")
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
