package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request payload for attendance records.
 */
public class AttendanceRequest {
  @NotNull
  public Long employeeId;
  @NotNull
  public LocalDate workDate;
  public LocalTime checkIn;
  public LocalTime checkOut;
  @NotBlank
  public String status;
  public String note;
}
