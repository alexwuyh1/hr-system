package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;

public class AttendanceRuleRequest {
  public String workStartTime;
  public String workEndTime;

  @NotNull public Integer lateGraceMinutes;
  public Integer absentThresholdMinutes;

  @NotNull public Integer overtimeThresholdMinutes;
}
