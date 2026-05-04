package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for attendance records.
 */
public class AttendanceRequest {
  @NotNull
  public Long employeeId;
  public String note;
}
