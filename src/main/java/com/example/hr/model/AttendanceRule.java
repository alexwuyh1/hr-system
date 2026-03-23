package com.example.hr.model;

import jakarta.persistence.*;

/**
 * Attendance rule configuration.
 */
@Entity
@Table(name = "attendance_rules")
public class AttendanceRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "late_grace_minutes", nullable = false)
  private Integer lateGraceMinutes;

  @Column(name = "overtime_threshold_minutes", nullable = false)
  private Integer overtimeThresholdMinutes;

  public Long getId() {
    return id;
  }

  public Integer getLateGraceMinutes() {
    return lateGraceMinutes;
  }

  public void setLateGraceMinutes(Integer lateGraceMinutes) {
    this.lateGraceMinutes = lateGraceMinutes;
  }

  public Integer getOvertimeThresholdMinutes() {
    return overtimeThresholdMinutes;
  }

  public void setOvertimeThresholdMinutes(Integer overtimeThresholdMinutes) {
    this.overtimeThresholdMinutes = overtimeThresholdMinutes;
  }
}
