package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for salary records.
 */
public class SalaryRequest {
  @NotNull
  public Long employeeId;
  @NotBlank
  public String salaryMonth; // YYYY-MM
  @NotNull
  public Double baseSalary;
  @NotNull
  public Double bonus;
  @NotNull
  public Double deduction;
  public String note;
}
