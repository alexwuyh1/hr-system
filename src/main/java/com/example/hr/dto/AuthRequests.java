package com.example.hr.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTOs for authentication endpoints.
 * Grouped in one file to keep the example concise.
 */
public class AuthRequests {
  public static class RegisterRequest {
    @NotBlank
    public String username;
    @NotBlank
    public String password;
  }

  public static class LoginRequest {
    @NotBlank
    public String username;
    @NotBlank
    public String password;
  }
}
