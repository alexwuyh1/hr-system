package com.example.hr.dto;

import com.example.hr.validation.Phone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * Request payload for creating/updating employees.
 */
public class EmployeeRequest {
    
    @NotBlank(message = "员工工号不能为空")
    public String employeeNo;
    
    @NotBlank(message = "员工姓名不能为空")
    public String name;
    
    @Phone
    public String phone;
    
    @Email(message = "邮箱格式不正确")
    public String email;
    
    @NotNull(message = "入职日期不能为空")
    @PastOrPresent(message = "入职日期不能晚于今天")
    public LocalDate hireDate;
    
    @NotBlank(message = "员工状态不能为空")
    public String status;
    
    public Long orgId;
    public Long positionId;
    public Long managerId;
}
