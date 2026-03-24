package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request payload for salary records.
 */
public class SalaryRequest {
  @NotNull
  public Long employeeId;
  @NotBlank
  @Pattern(regexp = "\\d{4}-\\d{2}", message = "salaryMonth must be in YYYY-MM format")
  public String salaryMonth; // YYYY-MM
  @NotNull
  @PositiveOrZero
  public Double baseSalary;
  @NotNull
  @PositiveOrZero
  public Double bonus;
  @NotNull
  @PositiveOrZero
  public Double deduction;
  public String note;
}
