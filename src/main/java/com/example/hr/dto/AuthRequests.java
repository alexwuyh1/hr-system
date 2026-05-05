package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthRequests {
  public static class RegisterRequest {
    @NotNull
    public Long employeeId;
    @NotBlank
    public String role;
  }

  public static class LoginRequest {
    @NotBlank
    public String username;
    @NotBlank
    public String password;
  }
}
