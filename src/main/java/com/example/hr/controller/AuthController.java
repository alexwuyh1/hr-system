package com.example.hr.controller;

import com.example.hr.dto.AuthRequests.LoginRequest;
import com.example.hr.dto.AuthRequests.RegisterRequest;
import com.example.hr.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints: register and login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
    Map<String, String> result = authService.login(request);
    return result;
  }
}
