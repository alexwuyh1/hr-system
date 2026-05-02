package com.example.hr.validation;

import com.example.hr.repository.EmployeeRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmployeeNoValidator implements ConstraintValidator<UniqueEmployeeNo, String> {
    
    private final EmployeeRepository employeeRepository;
    private long excludeId = -1;

    public UniqueEmployeeNoValidator(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void initialize(UniqueEmployeeNo annotation) {
        this.excludeId = annotation.excludeId();
    }

    @Override
    public boolean isValid(String employeeNo, ConstraintValidatorContext context) {
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            return true; // 由@NotBlank 处理
        }
        
        var existing = employeeRepository.findByEmployeeNo(employeeNo);
        if (existing.isPresent() && existing.get().getId() != excludeId) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("员工工号 '" + employeeNo + "' 已存在")
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
