package com.example.hr.dto;

import com.example.hr.model.Employee;

/**
 * Response DTO for employee to avoid deep object graphs in JSON.
 */
public class EmployeeResponse {
  public Long id;
  public String employeeNo;
  public String name;
  public String department;
  public String title;
  public String phone;
  public String email;
  public String hireDate;
  public String status;
  public Long departmentId;
  public String departmentName;
  public Long positionId;
  public String positionName;
  public Long gradeId;
  public String gradeName;
  public Long managerId;
  public String managerName;

  public static EmployeeResponse from(Employee employee) {
    EmployeeResponse r = new EmployeeResponse();
    r.id = employee.getId();
    r.employeeNo = employee.getEmployeeNo();
    r.name = employee.getName();
    r.department = employee.getDepartment();
    r.title = employee.getTitle();
    r.phone = employee.getPhone();
    r.email = employee.getEmail();
    r.hireDate = employee.getHireDate() == null ? null : employee.getHireDate().toString();
    r.status = employee.getStatus();
    if (employee.getDepartmentRef() != null) {
      r.departmentId = employee.getDepartmentRef().getId();
      r.departmentName = employee.getDepartmentRef().getName();
    }
    if (employee.getPositionRef() != null) {
      r.positionId = employee.getPositionRef().getId();
      r.positionName = employee.getPositionRef().getName();
    }
    if (employee.getGradeRef() != null) {
      r.gradeId = employee.getGradeRef().getId();
      r.gradeName = employee.getGradeRef().getName();
    }
    if (employee.getManagerRef() != null) {
      r.managerId = employee.getManagerRef().getId();
      r.managerName = employee.getManagerRef().getName();
    }
    return r;
  }
}
