package com.example.hr.controller;

import com.example.hr.dto.AuthRequests.LoginRequest;
import com.example.hr.dto.AuthRequests.RegisterRequest;
import com.example.hr.model.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final UserRepository userRepository;

  public AuthController(AuthService authService, UserRepository userRepository) {
    this.authService = authService;
    this.userRepository = userRepository;
  }

  @PostMapping("/register")
  public Map<String, String> register(@Valid @RequestBody RegisterRequest request,
                                       @RequestAttribute("userId") Long userId) {
    User caller = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    if (!"管理员".equals(caller.getRole())) {
      throw new IllegalArgumentException("只有管理员才能注册账号");
    }
    return authService.register(request);
  }

  @PostMapping("/login")
  public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
    Map<String, String> result = authService.login(request);
    return result;
  }
}
