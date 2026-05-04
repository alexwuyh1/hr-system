package com.example.hr.seed;

import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Employee;
import com.example.hr.model.Organization;
import com.example.hr.model.Permission;
import com.example.hr.model.RoleConfig;
import com.example.hr.model.User;
import com.example.hr.repository.AttendanceRuleRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.OrganizationRepository;
import com.example.hr.repository.PermissionRepository;
import com.example.hr.repository.RoleConfigRepository;
import com.example.hr.repository.UserRepository;
import java.time.LocalDate;
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
      RoleConfigRepository roleConfigRepository,
      OrganizationRepository organizationRepository,
      AttendanceRuleRepository attendanceRuleRepository,
      PasswordEncoder passwordEncoder) {
    return args -> {
      if (userRepository.count() == 0) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole("管理员");
        admin.setCreatedAt(System.currentTimeMillis());
        userRepository.save(admin);
      }

      if (roleConfigRepository.count() == 0) {
        RoleConfig hr = new RoleConfig();
        hr.setRole("人事");
        hr.setRoleMode("blacklist");
        roleConfigRepository.save(hr);

        RoleConfig emp = new RoleConfig();
        emp.setRole("员工");
        emp.setRoleMode("whitelist");
        roleConfigRepository.save(emp);
      }

      if (permissionRepository.count() == 0) {
        seedPermission(permissionRepository, "人事", "DELETE", "/api/permissions");
        seedPermission(permissionRepository, "人事", "GET", "/api/permissions");
        seedPermission(permissionRepository, "人事", "POST", "/api/permissions");
        seedPermission(permissionRepository, "人事", "PUT", "/api/permissions");
        seedPermission(permissionRepository, "人事", "GET", "/api/permissions/roles");
        seedPermission(permissionRepository, "人事", "POST", "/api/permissions/role");
        seedPermission(permissionRepository, "人事", "DELETE", "/api/permissions/role");

        seedPermission(permissionRepository, "员工", "POST", "/api/face/verify");
        seedPermission(permissionRepository, "员工", "POST", "/api/face/checkin");
        seedPermission(permissionRepository, "员工", "POST", "/api/face/checkout");
        seedPermission(permissionRepository, "员工", "POST", "/api/face/attendance");
      }

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
      String role,
      String method,
      String pathPrefix) {
    if (!permissionRepository.existsByRoleAndMethodAndPathPrefix(role, method, pathPrefix)) {
      Permission permission = new Permission();
      permission.setRole(role);
      permission.setMethod(method);
      permission.setPathPrefix(pathPrefix);
      permissionRepository.save(permission);
    }
  }
}
