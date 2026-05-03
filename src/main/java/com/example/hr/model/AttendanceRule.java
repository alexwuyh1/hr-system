package com.example.hr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "attendance_rules")
public class AttendanceRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "work_start_time")
  private String workStartTime;

  @Column(name = "work_end_time")
  private String workEndTime;

  @Column(name = "late_grace_minutes", nullable = false)
  private Integer lateGraceMinutes;

  @Column(name = "absent_threshold_minutes")
  private Integer absentThresholdMinutes;

  public Long getId() {
    return id;
  }

  public String getWorkStartTime() {
    return workStartTime;
  }

  public void setWorkStartTime(String workStartTime) {
    this.workStartTime = workStartTime;
  }

  public String getWorkEndTime() {
    return workEndTime;
  }

  public void setWorkEndTime(String workEndTime) {
    this.workEndTime = workEndTime;
  }

  public Integer getLateGraceMinutes() {
    return lateGraceMinutes;
  }

  public void setLateGraceMinutes(Integer lateGraceMinutes) {
    this.lateGraceMinutes = lateGraceMinutes;
  }

  public Integer getAbsentThresholdMinutes() {
    return absentThresholdMinutes;
  }

  public void setAbsentThresholdMinutes(Integer absentThresholdMinutes) {
    this.absentThresholdMinutes = absentThresholdMinutes;
  }
}
