package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Overtime request DTO.
 */
public class OvertimeRequestDto {
  @NotNull public Long employeeId;
  @NotNull public LocalDate workDate;
  @NotNull public Integer minutes;
  @NotBlank public String status; // PENDING / APPROVED / REJECTED
  public String note;
}
