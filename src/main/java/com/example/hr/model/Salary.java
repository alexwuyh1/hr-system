package com.example.hr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Salary record for an employee and month.
 * The salary_month is in YYYY-MM format.
 */
@Entity
@Table(name = "salaries")
public class Salary {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // EAGER fetch to avoid lazy proxy serialization errors in simple demo.
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "employee_id", nullable = false)
  @JsonIgnoreProperties({
    "hibernateLazyInitializer",
    "handler",
    "departmentRef",
    "positionRef",
    "gradeRef",
    "managerRef"
  })
  private Employee employee;

  @Column(name = "salary_month", nullable = false)
  private String salaryMonth;

  @Column(name = "base_salary", nullable = false)
  private Double baseSalary;

  @Column(nullable = false)
  private Double bonus;

  @Column(nullable = false)
  private Double deduction;

  private String note;

  public Long getId() {
    return id;
  }

  public Employee getEmployee() {
    return employee;
  }

  public void setEmployee(Employee employee) {
    this.employee = employee;
  }

  public String getSalaryMonth() {
    return salaryMonth;
  }

  public void setSalaryMonth(String salaryMonth) {
    this.salaryMonth = salaryMonth;
  }

  public Double getBaseSalary() {
    return baseSalary;
  }

  public void setBaseSalary(Double baseSalary) {
    this.baseSalary = baseSalary;
  }

  public Double getBonus() {
    return bonus;
  }

  public void setBonus(Double bonus) {
    this.bonus = bonus;
  }

  public Double getDeduction() {
    return deduction;
  }

  public void setDeduction(Double deduction) {
    this.deduction = deduction;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
