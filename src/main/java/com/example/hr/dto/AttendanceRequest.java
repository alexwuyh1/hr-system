package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
  @Pattern(regexp = "Normal|Late|Absent|Leave", message = "Invalid attendance status")
  public String status;
  public String note;
}
