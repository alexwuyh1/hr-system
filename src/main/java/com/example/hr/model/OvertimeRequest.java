package com.example.hr.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Overtime request with approval status.
 */
@Entity
@Table(name = "overtime_requests")
public class OvertimeRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  // Requested overtime minutes
  @Column(nullable = false)
  private Integer minutes;

  // PENDING, APPROVED, REJECTED
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

  public Integer getMinutes() {
    return minutes;
  }

  public void setMinutes(Integer minutes) {
    this.minutes = minutes;
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
