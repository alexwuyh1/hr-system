package com.example.hr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @Column(name = "employee_no", nullable = false, unique = true)
  private String employeeNo;

  @Column(nullable = false)
  private String name;

  private String phone;
  private String email;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @Column(nullable = false)
  private String status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "org_id")
  @JsonIgnoreProperties({"parent"})
  private Organization orgRef;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "position_id")
  @JsonIgnoreProperties({"parent", "grade"})
  private Organization positionRef;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_id")
  @JsonIgnoreProperties({"managerRef", "orgRef"})
  private Employee managerRef;

  @Column(name = "avatar_path")
  private String avatarPath;

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

  public Organization getOrgRef() {
    return orgRef;
  }

  public void setOrgRef(Organization orgRef) {
    this.orgRef = orgRef;
  }

  public Organization getPositionRef() {
    return positionRef;
  }

  public void setPositionRef(Organization positionRef) {
    this.positionRef = positionRef;
  }

  public Employee getManagerRef() {
    return managerRef;
  }

  public void setManagerRef(Employee managerRef) {
    this.managerRef = managerRef;
  }

  public String getAvatarPath() {
    return avatarPath;
  }

  public void setAvatarPath(String avatarPath) {
    this.avatarPath = avatarPath;
  }
}
