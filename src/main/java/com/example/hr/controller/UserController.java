package com.example.hr.controller;

import com.example.hr.model.User;
import com.example.hr.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * User management endpoints.
 * Only ADMIN can access these endpoints (enforced by AuthInterceptor).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<User> list() {
    return userRepository.findAll();
  }

  @PutMapping("/{id}/role")
  public Map<String, String> updateRole(
      @PathVariable("id") Long id, @RequestBody RoleRequest request) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setRole(request.role);
    userRepository.save(user);
    return Map.of("message", "Role updated");
  }

  public static class RoleRequest {
    @NotBlank public String role;
  }
}
