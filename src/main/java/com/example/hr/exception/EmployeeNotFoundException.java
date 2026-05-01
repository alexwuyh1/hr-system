package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class EmployeeNotFoundException extends BusinessException {
    public EmployeeNotFoundException(String employeeNo) {
        super("EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND, "员工不存在：" + employeeNo);
    }

    public EmployeeNotFoundException(Long id) {
        super("EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND, "员工不存在，ID: " + id);
    }
}
