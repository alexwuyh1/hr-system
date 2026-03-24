package com.example.hr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

  // 在职 / 离职等状态
  @Column(nullable = false)
  private String status;

  // Department tree reference (optional for legacy data)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "department_id")
  @JsonIgnoreProperties({"parent"})
  private Department departmentRef;

  // Position catalog reference
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "position_id")
  private Position positionRef;

  // Grade/level reference
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "grade_id")
  private Grade gradeRef;

  // Direct manager (self-reference)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "manager_id")
  @JsonIgnoreProperties({"managerRef", "departmentRef", "positionRef", "gradeRef"})
  private Employee managerRef;

  // Avatar file path on server
  @Column(name = "avatar_path")
  private String avatarPath;

  // Face hash (simple aHash for MVP)
  @Column(name = "face_hash")
  private String faceHash;

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

  public Department getDepartmentRef() {
    return departmentRef;
  }

  public void setDepartmentRef(Department departmentRef) {
    this.departmentRef = departmentRef;
  }

  public Position getPositionRef() {
    return positionRef;
  }

  public void setPositionRef(Position positionRef) {
    this.positionRef = positionRef;
  }

  public Grade getGradeRef() {
    return gradeRef;
  }

  public void setGradeRef(Grade gradeRef) {
    this.gradeRef = gradeRef;
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

  public String getFaceHash() {
    return faceHash;
  }

  public void setFaceHash(String faceHash) {
    this.faceHash = faceHash;
  }
}
