package com.example.hr.dto;

import com.example.hr.validation.EmployeeNoFormat;
import com.example.hr.validation.Phone;
import com.example.hr.validation.UniqueEmployeeNo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/**
 * Request payload for creating/updating employees.
 */
public class EmployeeRequest {
    
    @NotBlank(message = "员工工号不能为空")
    @EmployeeNoFormat
    @UniqueEmployeeNo
    public String employeeNo;
    
    @NotBlank(message = "员工姓名不能为空")
    public String name;
    
    @NotBlank(message = "部门不能为空")
    public String department;
    
    @NotBlank(message = "职位不能为空")
    public String title;
    
    @Phone
    public String phone;
    
    @Email(message = "邮箱格式不正确")
    public String email;
    
    @NotNull(message = "入职日期不能为空")
    @Past(message = "入职日期必须是过去的日期")
    public LocalDate hireDate;
    
    @NotBlank(message = "员工状态不能为空")
    public String status;
    
    public Long departmentId;
    public Long positionId;
    public Long gradeId;
    public Long managerId;
}
