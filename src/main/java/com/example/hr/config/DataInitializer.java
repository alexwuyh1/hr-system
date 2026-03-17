package com.example.hr.config;

import com.example.hr.model.Employee;
import com.example.hr.model.User;
import com.example.hr.repository.EmployeeRepository;
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
}
