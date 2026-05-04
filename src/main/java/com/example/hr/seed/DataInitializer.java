package com.example.hr.seed;

import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Employee;
import com.example.hr.model.Organization;
import com.example.hr.model.Permission;
import com.example.hr.model.User;
import com.example.hr.repository.AttendanceRuleRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.OrganizationRepository;
import com.example.hr.repository.PermissionRepository;
import com.example.hr.repository.UserRepository;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
  @Bean
  @Order(1)
  CommandLineRunner initData(
      UserRepository userRepository,
      EmployeeRepository employeeRepository,
      PermissionRepository permissionRepository,
      OrganizationRepository organizationRepository,
      AttendanceRuleRepository attendanceRuleRepository,
      DataSource dataSource,
      PasswordEncoder passwordEncoder) {
    return args -> {
      ensureEmployeeColumns(dataSource);
      ensureAttendanceColumns(dataSource);
      ensurePermissionColumns(dataSource);

      if (userRepository.count() == 0) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole("管理员");
        admin.setCreatedAt(System.currentTimeMillis());
        userRepository.save(admin);
      }

      String[] adminRoles = {"管理员"};
      String[] hrRoles = {"管理员", "人事"};
      String[] employeeRoles = {"管理员", "人事", "员工"};

      seedPermission(permissionRepository, "GET", "/api/employees", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/employees", hrRoles, "allow");
      seedPermission(permissionRepository, "PUT", "/api/employees", hrRoles, "allow");
      seedPermission(permissionRepository, "DELETE", "/api/employees", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/employees/resign", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/employees/rehire", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/employees/{id}/avatar", hrRoles, "allow");
      seedPermission(permissionRepository, "GET", "/api/employees/{id}/avatar", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/attendance", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/attendance", hrRoles, "allow");
      seedPermission(permissionRepository, "PUT", "/api/attendance", hrRoles, "allow");
      seedPermission(permissionRepository, "DELETE", "/api/attendance", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/attendance-rules", hrRoles, "allow");
      seedPermission(permissionRepository, "PUT", "/api/attendance-rules", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/attendance-rules", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/salaries", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/salaries", hrRoles, "allow");
      seedPermission(permissionRepository, "PUT", "/api/salaries", hrRoles, "allow");
      seedPermission(permissionRepository, "DELETE", "/api/salaries", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/organizations", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/organizations", hrRoles, "allow");
      seedPermission(permissionRepository, "PUT", "/api/organizations", hrRoles, "allow");
      seedPermission(permissionRepository, "DELETE", "/api/organizations", hrRoles, "allow");

      seedPermission(permissionRepository, "POST", "/api/face/verify", employeeRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/face/checkin", employeeRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/face/checkout", employeeRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/face/attendance", employeeRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/data", hrRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/data", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/dashboard", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/reports", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/init", hrRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/users", adminRoles, "allow");
      seedPermission(permissionRepository, "PUT", "/api/users", adminRoles, "allow");

      seedPermission(permissionRepository, "GET", "/api/permissions", adminRoles, "allow");
      seedPermission(permissionRepository, "POST", "/api/permissions", adminRoles, "allow");
      seedPermission(permissionRepository, "PUT", "/api/permissions", adminRoles, "allow");
      seedPermission(permissionRepository, "DELETE", "/api/permissions", adminRoles, "allow");
      seedPermission(permissionRepository, "GET", "/api/permissions/roles", adminRoles, "allow");

      if (organizationRepository.count() == 0) {
        Organization hq = new Organization();
        hq.setName("总部");
        hq.setType("部门");
        hq.setLevel(1);
        organizationRepository.save(hq);

        Organization hr = new Organization();
        hr.setName("人力资源部");
        hr.setType("部门");
        hr.setParent(hq);
        hr.setLevel(2);
        organizationRepository.save(hr);

        Organization eng = new Organization();
        eng.setName("研发部");
        eng.setType("部门");
        eng.setParent(hq);
        eng.setLevel(2);
        organizationRepository.save(eng);

        Organization fin = new Organization();
        fin.setName("财务部");
        fin.setType("部门");
        fin.setParent(hq);
        fin.setLevel(2);
        organizationRepository.save(fin);

        Organization g1 = new Organization();
        g1.setName("P3");
        g1.setType("职级");
        g1.setLevel(3);
        organizationRepository.save(g1);

        Organization g2 = new Organization();
        g2.setName("P4");
        g2.setType("职级");
        g2.setLevel(4);
        organizationRepository.save(g2);

        Organization p1 = new Organization();
        p1.setName("人力资源经理");
        p1.setType("岗位");
        p1.setParent(hr);
        p1.setGrade(g1);
        organizationRepository.save(p1);

        Organization p2 = new Organization();
        p2.setName("软件工程师");
        p2.setType("岗位");
        p2.setParent(eng);
        p2.setGrade(g2);
        organizationRepository.save(p2);
      }

      if (employeeRepository.count() == 0) {
        Organization hr = organizationRepository.findAll().stream().filter(o -> "人力资源部".equals(o.getName())).findFirst().orElse(null);
        Organization eng = organizationRepository.findAll().stream().filter(o -> "研发部".equals(o.getName())).findFirst().orElse(null);
        Organization hrMgrPos = organizationRepository.findAll().stream().filter(o -> "人力资源经理".equals(o.getName())).findFirst().orElse(null);
        Organization engPos = organizationRepository.findAll().stream().filter(o -> "软件工程师".equals(o.getName())).findFirst().orElse(null);

        Employee e1 = new Employee();
        e1.setEmployeeNo("E1001");
        e1.setName("Alice Chen");
        e1.setPhone("13800000001");
        e1.setEmail("alice@example.com");
        e1.setHireDate(LocalDate.now().minusYears(2));
        e1.setStatus("在职");
        e1.setOrgRef(hr);
        e1.setPositionRef(hrMgrPos);
        employeeRepository.save(e1);

        Employee e2 = new Employee();
        e2.setEmployeeNo("E1002");
        e2.setName("Bob Li");
        e2.setPhone("13800000002");
        e2.setEmail("bob@example.com");
        e2.setHireDate(LocalDate.now().minusYears(1));
        e2.setStatus("在职");
        e2.setOrgRef(eng);
        e2.setPositionRef(engPos);
        e2.setManagerRef(e1);
        employeeRepository.save(e2);
      }

      if (attendanceRuleRepository.count() == 0) {
        AttendanceRule rule = new AttendanceRule();
        rule.setLateGraceMinutes(10);
        attendanceRuleRepository.save(rule);
      }
    };
  }

  private void seedPermission(
      PermissionRepository permissionRepository,
      String method,
      String pathPrefix,
      String[] roles,
      String mode) {
    for (String role : roles) {
      if (!permissionRepository.existsByRoleAndMethodAndPathPrefixAndMode(role, method, pathPrefix, mode)) {
        Permission permission = new Permission();
        permission.setRole(role);
        permission.setMethod(method);
        permission.setPathPrefix(pathPrefix);
        permission.setMode(mode);
        permission.setRoleMode("whitelist");
        permissionRepository.save(permission);
      }
    }
  }

  private void ensureEmployeeColumns(DataSource dataSource) {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery("PRAGMA table_info(employees)");
      boolean hasOrgId = false;
      boolean hasPositionId = false;
      boolean hasManagerId = false;
      boolean hasAvatar = false;
      boolean hasFaceHash = false;
      boolean hasPhone = false;
      boolean hasEmail = false;
      while (rs.next()) {
        String name = rs.getString("name");
        if ("org_id".equals(name)) hasOrgId = true;
        if ("position_id".equals(name)) hasPositionId = true;
        if ("manager_id".equals(name)) hasManagerId = true;
        if ("avatar_path".equals(name)) hasAvatar = true;
        if ("face_hash".equals(name)) hasFaceHash = true;
        if ("phone".equals(name)) hasPhone = true;
        if ("email".equals(name)) hasEmail = true;
      }
      if (!hasOrgId) {
        stmt.execute("ALTER TABLE employees ADD COLUMN org_id INTEGER");
      }
      if (!hasPositionId) {
        stmt.execute("ALTER TABLE employees ADD COLUMN position_id INTEGER");
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
      if (!hasPhone) {
        stmt.execute("ALTER TABLE employees ADD COLUMN phone TEXT");
      }
      if (!hasEmail) {
        stmt.execute("ALTER TABLE employees ADD COLUMN email TEXT");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void ensureAttendanceColumns(DataSource dataSource) {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery("PRAGMA table_info(attendance)");
      boolean hasLate = false;
      while (rs.next()) {
        String name = rs.getString("name");
        if ("late_minutes".equals(name)) hasLate = true;
      }
      if (!hasLate) {
        stmt.execute("ALTER TABLE attendance ADD COLUMN late_minutes INTEGER");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void ensurePermissionColumns(DataSource dataSource) {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery("PRAGMA table_info(permissions)");
      boolean hasMode = false;
      boolean hasRoleMode = false;
      while (rs.next()) {
        String name = rs.getString("name");
        if ("mode".equals(name)) hasMode = true;
        if ("role_mode".equals(name)) hasRoleMode = true;
      }
      if (!hasMode) {
        stmt.execute("ALTER TABLE permissions ADD COLUMN mode TEXT DEFAULT 'allow'");
      }
      if (!hasRoleMode) {
        stmt.execute("ALTER TABLE permissions ADD COLUMN role_mode TEXT DEFAULT 'whitelist'");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
