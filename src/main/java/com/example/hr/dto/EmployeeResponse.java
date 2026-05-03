package com.example.hr.dto;

import com.example.hr.model.Employee;

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
  public Long orgId;
  public String orgName;
  public Long managerId;
  public String managerName;
  public String avatarUrl;

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
    if (employee.getOrgRef() != null) {
      r.orgId = employee.getOrgRef().getId();
      r.orgName = employee.getOrgRef().getName();
    }
    if (employee.getManagerRef() != null) {
      r.managerId = employee.getManagerRef().getId();
      r.managerName = employee.getManagerRef().getName();
    }
    if (employee.getAvatarPath() != null) {
      r.avatarUrl = "/api/employees/" + employee.getId() + "/avatar";
    }
    return r;
  }
}
