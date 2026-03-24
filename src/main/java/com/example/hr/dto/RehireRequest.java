package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for employee rehire.
 */
public class RehireRequest {
  @NotBlank
  public String employeeNo;
}
