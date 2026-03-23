package com.example.hr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Daily attendance record.
 * A unique record per employee per work date.
 */
@Entity
@Table(name = "attendance")
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Link to employee
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

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(name = "check_in")
  private LocalTime checkIn;

  @Column(name = "check_out")
  private LocalTime checkOut;

  // Normal, Late, Absent, Leave, etc.
  @Column(nullable = false)
  private String status;

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

  public LocalDate getWorkDate() {
    return workDate;
  }

  public void setWorkDate(LocalDate workDate) {
    this.workDate = workDate;
  }

  public LocalTime getCheckIn() {
    return checkIn;
  }

  public void setCheckIn(LocalTime checkIn) {
    this.checkIn = checkIn;
  }

  public LocalTime getCheckOut() {
    return checkOut;
  }

  public void setCheckOut(LocalTime checkOut) {
    this.checkOut = checkOut;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
