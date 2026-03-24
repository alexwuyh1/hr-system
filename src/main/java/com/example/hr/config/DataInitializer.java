package com.example.hr.config;

import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Department;
import com.example.hr.model.Employee;
import com.example.hr.model.Grade;
import com.example.hr.model.Permission;
import com.example.hr.model.Position;
import com.example.hr.model.Role;
import com.example.hr.model.User;
import com.example.hr.repository.AttendanceRuleRepository;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.GradeRepository;
import com.example.hr.repository.PermissionRepository;
import com.example.hr.repository.PositionRepository;
import com.example.hr.repository.RoleRepository;
import com.example.hr.repository.UserRepository;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
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
      DepartmentRepository departmentRepository,
      PositionRepository positionRepository,
      GradeRepository gradeRepository,
      AttendanceRuleRepository attendanceRuleRepository,
      DataSource dataSource,
      PasswordEncoder passwordEncoder) {
    return args -> {
      ensureEmployeeColumns(dataSource);
      ensureAttendanceColumns(dataSource);

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
      // Dashboard permissions
      seedPermission(permissionRepository, "GET", "/api/dashboard", "ADMIN", "HR", "FINANCE", "MANAGER");
      // Init aggregation permissions
      seedPermission(permissionRepository, "GET", "/api/init", "ADMIN", "HR", "FINANCE", "MANAGER");

      // User management (admin only)
      seedPermission(permissionRepository, "GET", "/api/users", "ADMIN");
      seedPermission(permissionRepository, "PUT", "/api/users", "ADMIN");
      seedPermission(permissionRepository, "POST", "/api/permissions", "ADMIN");
      seedPermission(permissionRepository, "PUT", "/api/permissions", "ADMIN");
      seedPermission(permissionRepository, "DELETE", "/api/permissions", "ADMIN");
      seedPermission(permissionRepository, "GET", "/api/permissions", "ADMIN");
      seedPermission(permissionRepository, "GET", "/api/roles", "ADMIN");
      seedPermission(permissionRepository, "GET", "/api/departments", "ADMIN", "HR", "MANAGER");
      seedPermission(permissionRepository, "POST", "/api/departments", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/departments", "ADMIN", "HR");
      seedPermission(permissionRepository, "DELETE", "/api/departments", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/positions", "ADMIN", "HR");
      seedPermission(permissionRepository, "POST", "/api/positions", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/positions", "ADMIN", "HR");
      seedPermission(permissionRepository, "DELETE", "/api/positions", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/grades", "ADMIN", "HR");
      seedPermission(permissionRepository, "POST", "/api/grades", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/grades", "ADMIN", "HR");
      seedPermission(permissionRepository, "DELETE", "/api/grades", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/departments/tree", "ADMIN", "HR", "MANAGER");
      seedPermission(permissionRepository, "GET", "/api/shifts", "ADMIN", "HR", "MANAGER");
      seedPermission(permissionRepository, "POST", "/api/shifts", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/shifts", "ADMIN", "HR");
      seedPermission(permissionRepository, "DELETE", "/api/shifts", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/leaves", "ADMIN", "HR");
      seedPermission(permissionRepository, "POST", "/api/leaves", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/leaves", "ADMIN", "HR");
      seedPermission(permissionRepository, "DELETE", "/api/leaves", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/overtime", "ADMIN", "HR", "MANAGER");
      seedPermission(permissionRepository, "POST", "/api/overtime", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/overtime", "ADMIN", "HR");
      seedPermission(permissionRepository, "DELETE", "/api/overtime", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/attendance-rules", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/attendance-rules", "ADMIN", "HR");
      seedPermission(permissionRepository, "POST", "/api/attendance-rules", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/data", "ADMIN", "HR", "MANAGER");
      seedPermission(permissionRepository, "POST", "/api/data", "ADMIN", "HR");
      seedPermission(permissionRepository, "GET", "/api/employees", "ADMIN", "HR", "MANAGER");
      seedPermission(permissionRepository, "POST", "/api/employees", "ADMIN", "HR");
      seedPermission(permissionRepository, "PUT", "/api/employees", "ADMIN", "HR");
      seedPermission(permissionRepository, "DELETE", "/api/employees", "ADMIN", "HR");
      seedPermission(permissionRepository, "POST", "/api/face", "ADMIN", "HR", "MANAGER");

      if (departmentRepository.count() == 0) {
        Department hq = new Department();
        hq.setName("Headquarters");
        departmentRepository.save(hq);

        Department hr = new Department();
        hr.setName("HR");
        hr.setParent(hq);
        departmentRepository.save(hr);

        Department eng = new Department();
        eng.setName("Engineering");
        eng.setParent(hq);
        departmentRepository.save(eng);

        Department fin = new Department();
        fin.setName("Finance");
        fin.setParent(hq);
        departmentRepository.save(fin);
      }

      if (positionRepository.count() == 0) {
        Position p1 = new Position();
        p1.setName("HR Manager");
        positionRepository.save(p1);

        Position p2 = new Position();
        p2.setName("Software Engineer");
        positionRepository.save(p2);

        Position p3 = new Position();
        p3.setName("Analyst");
        positionRepository.save(p3);
      }

      if (gradeRepository.count() == 0) {
        Grade g1 = new Grade();
        g1.setName("P3");
        g1.setLevel(3);
        gradeRepository.save(g1);

        Grade g2 = new Grade();
        g2.setName("P4");
        g2.setLevel(4);
        gradeRepository.save(g2);
      }

      if (employeeRepository.count() == 0) {
        Department hr = departmentRepository.findAll().stream().filter(d -> "HR".equals(d.getName())).findFirst().orElse(null);
        Department eng = departmentRepository.findAll().stream().filter(d -> "Engineering".equals(d.getName())).findFirst().orElse(null);
        Position hrMgr = positionRepository.findAll().stream().filter(p -> "HR Manager".equals(p.getName())).findFirst().orElse(null);
        Position engPos = positionRepository.findAll().stream().filter(p -> "Software Engineer".equals(p.getName())).findFirst().orElse(null);
        Grade g4 = gradeRepository.findAll().stream().filter(g -> g.getLevel() == 4).findFirst().orElse(null);
        Grade g3 = gradeRepository.findAll().stream().filter(g -> g.getLevel() == 3).findFirst().orElse(null);

        Employee e1 = new Employee();
        e1.setEmployeeNo("E1001");
        e1.setName("Alice Chen");
        e1.setDepartment("HR");
        e1.setTitle("HR Manager");
        e1.setPhone("13800000001");
        e1.setEmail("alice@example.com");
        e1.setHireDate(LocalDate.now().minusYears(2));
        e1.setStatus("Active");
        e1.setDepartmentRef(hr);
        e1.setPositionRef(hrMgr);
        e1.setGradeRef(g4);
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
        e2.setDepartmentRef(eng);
        e2.setPositionRef(engPos);
        e2.setGradeRef(g3);
        e2.setManagerRef(e1);
        employeeRepository.save(e2);
      }

      if (attendanceRuleRepository.count() == 0) {
        AttendanceRule rule = new AttendanceRule();
        rule.setLateGraceMinutes(10);
        rule.setOvertimeThresholdMinutes(30);
        attendanceRuleRepository.save(rule);
      }
    };
  }

  private void seedPermission(
      PermissionRepository permissionRepository,
      String method,
      String pathPrefix,
      String... roles) {
    for (String role : roles) {
      if (!permissionRepository.existsByRoleAndMethodAndPathPrefix(role, method, pathPrefix)) {
        Permission permission = new Permission();
        permission.setRole(role);
        permission.setMethod(method);
        permission.setPathPrefix(pathPrefix);
        permissionRepository.save(permission);
      }
    }
  }

  private void ensureEmployeeColumns(DataSource dataSource) {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery("PRAGMA table_info(employees)");
      boolean hasDeptId = false;
      boolean hasPositionId = false;
      boolean hasGradeId = false;
      boolean hasManagerId = false;
      boolean hasAvatar = false;
      boolean hasFaceHash = false;
      while (rs.next()) {
        String name = rs.getString("name");
        if ("department_id".equals(name)) hasDeptId = true;
        if ("position_id".equals(name)) hasPositionId = true;
        if ("grade_id".equals(name)) hasGradeId = true;
        if ("manager_id".equals(name)) hasManagerId = true;
        if ("avatar_path".equals(name)) hasAvatar = true;
        if ("face_hash".equals(name)) hasFaceHash = true;
      }
      if (!hasDeptId) {
        stmt.execute("ALTER TABLE employees ADD COLUMN department_id INTEGER");
      }
      if (!hasPositionId) {
        stmt.execute("ALTER TABLE employees ADD COLUMN position_id INTEGER");
      }
      if (!hasGradeId) {
        stmt.execute("ALTER TABLE employees ADD COLUMN grade_id INTEGER");
      }
      if (!hasManagerId) {
        stmt.execute("ALTER TABLE employees ADD COLUMN manager_id INTEGER");
      }
      if (!hasAvatar) {
        stmt.execute("ALTER TABLE employees ADD COLUMN avatar_path TEXT");
      }
      if (!hasFaceHash) {
        stmt.execute("ALTER TABLE employees ADD COLUMN face_hash TEXT");
      }
    } catch (Exception ex) {
      // best-effort migration for demo; log and continue
      ex.printStackTrace();
    }
  }

  private void ensureAttendanceColumns(DataSource dataSource) {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery("PRAGMA table_info(attendance)");
      boolean hasLate = false;
      boolean hasOvertime = false;
      while (rs.next()) {
        String name = rs.getString("name");
        if ("late_minutes".equals(name)) hasLate = true;
        if ("overtime_minutes".equals(name)) hasOvertime = true;
      }
      if (!hasLate) {
        stmt.execute("ALTER TABLE attendance ADD COLUMN late_minutes INTEGER");
      }
      if (!hasOvertime) {
        stmt.execute("ALTER TABLE attendance ADD COLUMN overtime_minutes INTEGER");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
