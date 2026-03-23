package com.example.hr.config;

import com.example.hr.model.Employee;
import com.example.hr.model.Permission;
import com.example.hr.model.Role;
import com.example.hr.model.User;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.PermissionRepository;
import com.example.hr.repository.RoleRepository;
import com.example.hr.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed initial data for a fresh database.
 * Creates a default admin user and a few sample employees.
 */
@Configuration
public class DataInitializer {
  @Bean
  CommandLineRunner initData(
      UserRepository userRepository,
      EmployeeRepository employeeRepository,
      RoleRepository roleRepository,
      PermissionRepository permissionRepository,
      PasswordEncoder passwordEncoder) {
    return args -> {
      if (userRepository.count() == 0) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        admin.setCreatedAt(System.currentTimeMillis());
        userRepository.save(admin);
      }

      if (roleRepository.count() == 0) {
        for (String roleName : new String[] {"ADMIN", "HR", "FINANCE", "MANAGER", "USER"}) {
          Role role = new Role();
          role.setName(roleName);
          roleRepository.save(role);
        }
      }

      if (permissionRepository.count() == 0) {
        // Employee permissions
        seedPermission(permissionRepository, "GET", "/api/employees", "ADMIN", "HR", "MANAGER");
        seedPermission(permissionRepository, "POST", "/api/employees", "ADMIN", "HR");
        seedPermission(permissionRepository, "PUT", "/api/employees", "ADMIN", "HR");
        seedPermission(permissionRepository, "DELETE", "/api/employees", "ADMIN", "HR");

        // Attendance permissions
        seedPermission(permissionRepository, "GET", "/api/attendance", "ADMIN", "HR", "MANAGER");
        seedPermission(permissionRepository, "POST", "/api/attendance", "ADMIN", "HR");
        seedPermission(permissionRepository, "PUT", "/api/attendance", "ADMIN", "HR");
        seedPermission(permissionRepository, "DELETE", "/api/attendance", "ADMIN", "HR");

        // Salary permissions
        seedPermission(permissionRepository, "GET", "/api/salaries", "ADMIN", "FINANCE");
        seedPermission(permissionRepository, "POST", "/api/salaries", "ADMIN", "FINANCE");
        seedPermission(permissionRepository, "PUT", "/api/salaries", "ADMIN", "FINANCE");
        seedPermission(permissionRepository, "DELETE", "/api/salaries", "ADMIN", "FINANCE");

        // Report permissions
        seedPermission(permissionRepository, "GET", "/api/reports", "ADMIN", "HR", "FINANCE", "MANAGER");

        // User management (admin only)
        seedPermission(permissionRepository, "GET", "/api/users", "ADMIN");
        seedPermission(permissionRepository, "PUT", "/api/users", "ADMIN");
        seedPermission(permissionRepository, "POST", "/api/permissions", "ADMIN");
        seedPermission(permissionRepository, "PUT", "/api/permissions", "ADMIN");
        seedPermission(permissionRepository, "DELETE", "/api/permissions", "ADMIN");
        seedPermission(permissionRepository, "GET", "/api/permissions", "ADMIN");
        seedPermission(permissionRepository, "GET", "/api/roles", "ADMIN");
      }

      if (employeeRepository.count() == 0) {
        Employee e1 = new Employee();
        e1.setEmployeeNo("E1001");
        e1.setName("Alice Chen");
        e1.setDepartment("HR");
        e1.setTitle("HR Manager");
        e1.setPhone("13800000001");
        e1.setEmail("alice@example.com");
        e1.setHireDate(LocalDate.now().minusYears(2));
        e1.setStatus("Active");
        employeeRepository.save(e1);

        Employee e2 = new Employee();
        e2.setEmployeeNo("E1002");
        e2.setName("Bob Li");
        e2.setDepartment("Engineering");
        e2.setTitle("Software Engineer");
        e2.setPhone("13800000002");
        e2.setEmail("bob@example.com");
        e2.setHireDate(LocalDate.now().minusYears(1));
        e2.setStatus("Active");
        employeeRepository.save(e2);
      }
    };
  }

  private void seedPermission(
      PermissionRepository permissionRepository,
      String method,
      String pathPrefix,
      String... roles) {
    for (String role : roles) {
      Permission permission = new Permission();
      permission.setRole(role);
      permission.setMethod(method);
      permission.setPathPrefix(pathPrefix);
      permissionRepository.save(permission);
    }
  }
}
