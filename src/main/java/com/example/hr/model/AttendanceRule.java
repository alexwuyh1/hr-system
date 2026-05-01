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

  @Column(name = "lunch_break_start")
  private String lunchBreakStart;

  @Column(name = "lunch_break_end")
  private String lunchBreakEnd;

  @Column(name = "late_grace_minutes", nullable = false)
  private Integer lateGraceMinutes;

  @Column(name = "early_leave_grace_minutes")
  private Integer earlyLeaveGraceMinutes;

  @Column(name = "absent_threshold_minutes")
  private Integer absentThresholdMinutes;

  @Column(name = "overtime_threshold_minutes", nullable = false)
  private Integer overtimeThresholdMinutes;

  @Column(name = "require_overtime_approval")
  private Boolean requireOvertimeApproval;

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

  public String getLunchBreakStart() {
    return lunchBreakStart;
  }

  public void setLunchBreakStart(String lunchBreakStart) {
    this.lunchBreakStart = lunchBreakStart;
  }

  public String getLunchBreakEnd() {
    return lunchBreakEnd;
  }

  public void setLunchBreakEnd(String lunchBreakEnd) {
    this.lunchBreakEnd = lunchBreakEnd;
  }

  public Integer getLateGraceMinutes() {
    return lateGraceMinutes;
  }

  public void setLateGraceMinutes(Integer lateGraceMinutes) {
    this.lateGraceMinutes = lateGraceMinutes;
  }

  public Integer getEarlyLeaveGraceMinutes() {
    return earlyLeaveGraceMinutes;
  }

  public void setEarlyLeaveGraceMinutes(Integer earlyLeaveGraceMinutes) {
    this.earlyLeaveGraceMinutes = earlyLeaveGraceMinutes;
  }

  public Integer getAbsentThresholdMinutes() {
    return absentThresholdMinutes;
  }

  public void setAbsentThresholdMinutes(Integer absentThresholdMinutes) {
    this.absentThresholdMinutes = absentThresholdMinutes;
  }

  public Integer getOvertimeThresholdMinutes() {
    return overtimeThresholdMinutes;
  }

  public void setOvertimeThresholdMinutes(Integer overtimeThresholdMinutes) {
    this.overtimeThresholdMinutes = overtimeThresholdMinutes;
  }

  public Boolean getRequireOvertimeApproval() {
    return requireOvertimeApproval;
  }

  public void setRequireOvertimeApproval(Boolean requireOvertimeApproval) {
    this.requireOvertimeApproval = requireOvertimeApproval;
  }
}
