package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class EmployeeNotActiveException extends BusinessException {
    public EmployeeNotActiveException(String employeeNo) {
        super("EMPLOYEE_NOT_ACTIVE", HttpStatus.BAD_REQUEST, "员工未在职：" + employeeNo);
    }

    public EmployeeNotActiveException(Long id) {
        super("EMPLOYEE_NOT_ACTIVE", HttpStatus.BAD_REQUEST, "员工未在职，ID: " + id);
    }
}
