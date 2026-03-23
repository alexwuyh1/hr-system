package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Leave request DTO.
 */
public class LeaveRequestDto {
  @NotNull public Long employeeId;
  @NotNull public LocalDate startDate;
  @NotNull public LocalDate endDate;
  @NotBlank public String type;
  @NotBlank public String status; // PENDING / APPROVED / REJECTED
  public String note;
}
