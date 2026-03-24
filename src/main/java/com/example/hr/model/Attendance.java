package com.example.hr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Daily attendance record.
 * Multiple records per employee per work date are allowed for multi-check-in/out.
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
  @JsonFormat(pattern = "HH:mm:ss")
  private LocalTime checkIn;

  @Column(name = "check_out")
  @JsonFormat(pattern = "HH:mm:ss")
  private LocalTime checkOut;

  // Normal, Late, Absent, Leave, etc.
  @Column(nullable = false)
  private String status;

  private String note;

  // Minutes late, computed by rule engine
  @Column(name = "late_minutes")
  private Integer lateMinutes;

  // Minutes overtime, computed by rule engine
  @Column(name = "overtime_minutes")
  private Integer overtimeMinutes;

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

  public Integer getLateMinutes() {
    return lateMinutes;
  }

  public void setLateMinutes(Integer lateMinutes) {
    this.lateMinutes = lateMinutes;
  }

  public Integer getOvertimeMinutes() {
    return overtimeMinutes;
  }

  public void setOvertimeMinutes(Integer overtimeMinutes) {
    this.overtimeMinutes = overtimeMinutes;
  }
}
