package com.example.hr.service;

import com.example.hr.dto.AuthRequests.LoginRequest;
import com.example.hr.dto.AuthRequests.RegisterRequest;
import com.example.hr.model.Employee;
import com.example.hr.model.User;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.RoleConfigRepository;
import com.example.hr.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;
  private final RoleConfigRepository roleConfigRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  public AuthService(
      UserRepository userRepository,
      EmployeeRepository employeeRepository,
      RoleConfigRepository roleConfigRepository,
      PasswordEncoder passwordEncoder,
      TokenService tokenService) {
    this.userRepository = userRepository;
    this.employeeRepository = employeeRepository;
    this.roleConfigRepository = roleConfigRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
  }

  public Map<String, String> register(RegisterRequest request) {
    roleConfigRepository.findByRole(request.role)
        .orElseThrow(() -> new IllegalArgumentException("角色不存在，请先在权限管理中创建角色"));
    Employee employee = employeeRepository.findById(request.employeeId)
        .orElseThrow(() -> new IllegalArgumentException("员工不存在"));
    if (!"在职".equals(employee.getStatus())) {
      throw new IllegalArgumentException("仅可为在职员工注册账号");
    }
    String username = "hr" + employee.getEmployeeNo();
    if (userRepository.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("该员工已有账号，用户名: " + username);
    }
    String password = "hr" + employee.getEmployeeNo() + "!";
    User user = new User();
    user.setUsername(username);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(request.role);
    user.setEmployee(employee);
    user.setCreatedAt(System.currentTimeMillis());
    userRepository.save(user);
    return Map.of("username", username, "password", password, "role", request.role);
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
