package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;

public class AttendanceRuleRequest {
  public String workStartTime;
  public String workEndTime;
  public String lunchBreakStart;
  public String lunchBreakEnd;

  @NotNull public Integer lateGraceMinutes;
  public Integer earlyLeaveGraceMinutes;
  public Integer absentThresholdMinutes;

  @NotNull public Integer overtimeThresholdMinutes;
  public Boolean requireOvertimeApproval;
}
