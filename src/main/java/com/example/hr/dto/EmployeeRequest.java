package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request payload for creating/updating employees.
 */
public class EmployeeRequest {
  @NotBlank
  public String employeeNo;
  @NotBlank
  public String name;
  @NotBlank
  public String department;
  @NotBlank
  public String title;
  public String phone;
  public String email;
  @NotNull
  public LocalDate hireDate;
  @NotBlank
  public String status;
}
