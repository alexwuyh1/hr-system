package com.example.hr.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Employee master data.
 * This table is the core reference for attendance and salary records.
 */
@Entity
@Table(name = "employees")
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Unique employee number used by HR
  @Column(name = "employee_no", nullable = false, unique = true)
  private String employeeNo;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String department;

  @Column(nullable = false)
  private String title;

  private String phone;
  private String email;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  // Active, OnLeave, Resigned, etc.
  @Column(nullable = false)
  private String status;

  public Long getId() {
    return id;
  }

  public String getEmployeeNo() {
    return employeeNo;
  }

  public void setEmployeeNo(String employeeNo) {
    this.employeeNo = employeeNo;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDate getHireDate() {
    return hireDate;
  }

  public void setHireDate(LocalDate hireDate) {
    this.hireDate = hireDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
