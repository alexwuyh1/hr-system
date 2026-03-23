package com.example.hr.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Shift request DTO.
 */
public class ShiftRequest {
  @NotNull public Long employeeId;
  @NotNull public LocalDate workDate;
  @NotNull public LocalTime startTime;
  @NotNull public LocalTime endTime;
  public String note;
}
