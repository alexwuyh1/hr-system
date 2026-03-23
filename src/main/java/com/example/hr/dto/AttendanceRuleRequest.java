package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Attendance rule request DTO.
 */
public class AttendanceRuleRequest {
  @NotNull public Integer lateGraceMinutes;
  @NotNull public Integer overtimeThresholdMinutes;
}
