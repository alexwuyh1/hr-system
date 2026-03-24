package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for employee resignation.
 */
public class ResignRequest {
  @NotBlank
  public String employeeNo;
}
