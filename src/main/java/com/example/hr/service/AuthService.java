package com.example.hr.service;

import com.example.hr.dto.AuthRequests.LoginRequest;
import com.example.hr.dto.AuthRequests.RegisterRequest;
import com.example.hr.model.User;
import com.example.hr.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Authentication service for user registration and login.
 * Uses BCrypt for password hashing and a token issued by TokenService.
 */
@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      TokenService tokenService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
  }

  public void register(RegisterRequest request) {
    userRepository
        .findByUsername(request.username)
        .ifPresent(u -> {
          throw new IllegalArgumentException("用户名已存在");
        });
    User user = new User();
    user.setUsername(request.username);
    user.setPasswordHash(passwordEncoder.encode(request.password));
    user.setRole(request.role);
    user.setCreatedAt(System.currentTimeMillis());
    userRepository.save(user);
  }

  public Map<String, String> login(LoginRequest request) {
    User user =
        userRepository
            .findByUsername(request.username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
    if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid username or password");
    }
    String token = tokenService.issueToken(user.getId());
    return Map.of("token", token, "role", user.getRole());
  }
}
