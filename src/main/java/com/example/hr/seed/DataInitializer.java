package com.example.hr.seed;

import com.example.hr.model.Attendance;
import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Employee;
import com.example.hr.model.Organization;
import com.example.hr.model.Permission;
import com.example.hr.model.RoleConfig;
import com.example.hr.model.Salary;
import com.example.hr.model.User;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.AttendanceRuleRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.OrganizationRepository;
import com.example.hr.repository.PermissionRepository;
import com.example.hr.repository.RoleConfigRepository;
import com.example.hr.repository.SalaryRepository;
import com.example.hr.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
      AttendanceRepository attendanceRepository,
      SalaryRepository salaryRepository,
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

        Organization p3 = new Organization();
        p3.setName("财务专员");
        p3.setType("岗位");
        p3.setParent(fin);
        p3.setGrade(g1);
        organizationRepository.save(p3);

        Organization p4 = new Organization();
        p4.setName("高级工程师");
        p4.setType("岗位");
        p4.setParent(eng);
        p4.setGrade(g2);
        organizationRepository.save(p4);
      }

      if (employeeRepository.count() == 0) {
        Organization hrDept = orgRepo(organizationRepository, "人力资源部");
        Organization engDept = orgRepo(organizationRepository, "研发部");
        Organization finDept = orgRepo(organizationRepository, "财务部");
        Organization hrMgrPos = orgRepo(organizationRepository, "人力资源经理");
        Organization engPos = orgRepo(organizationRepository, "软件工程师");
        Organization srEngPos = orgRepo(organizationRepository, "高级工程师");
        Organization finPos = orgRepo(organizationRepository, "财务专员");

        Employee alice = seedEmp(employeeRepository, "E1001", "Alice Chen", "13800000001", "alice@example.com",
            LocalDate.now().minusYears(3), "在职", hrDept, hrMgrPos, null);

        Employee bob = seedEmp(employeeRepository, "E1002", "Bob Li", "13800000002", "bob@example.com",
            LocalDate.now().minusYears(1), "在职", engDept, engPos, alice);

        Employee carol = seedEmp(employeeRepository, "E1003", "Carol Wang", "13800000003", "carol@example.com",
            LocalDate.now().minusYears(2), "在职", engDept, srEngPos, alice);

        Employee dave = seedEmp(employeeRepository, "E1004", "Dave Zhang", "13800000004", "dave@example.com",
            LocalDate.now().minusMonths(6), "在职", finDept, finPos, alice);

        Employee eve = seedEmp(employeeRepository, "E1005", "Eve Liu", "13800000005", "eve@example.com",
            LocalDate.now().minusMonths(3), "在职", engDept, engPos, carol);

        Employee frank = seedEmp(employeeRepository, "E1006", "Frank Wu", "13800000006", "frank@example.com",
            LocalDate.now().minusYears(4), "离职", hrDept, hrMgrPos, null);

        Employee grace = seedEmp(employeeRepository, "E1007", "Grace Zhao", "13800000007", "grace@example.com",
            LocalDate.now().minusMonths(1), "在职", finDept, finPos, dave);
      }

      if (attendanceRuleRepository.count() == 0) {
        AttendanceRule rule = new AttendanceRule();
        rule.setLateGraceMinutes(10);
        attendanceRuleRepository.save(rule);
      }

      if (salaryRepository.count() == 0) {
        for (Employee emp : employeeRepository.findAll()) {
          if (!"在职".equals(emp.getStatus())) continue;
          seedSalary(salaryRepository, emp, LocalDate.now().minusMonths(0));
          seedSalary(salaryRepository, emp, LocalDate.now().minusMonths(1));
          seedSalary(salaryRepository, emp, LocalDate.now().minusMonths(2));
        }
      }

      if (attendanceRepository.count() == 0) {
        for (Employee emp : employeeRepository.findAll()) {
          if (!"在职".equals(emp.getStatus())) continue;
          for (int d = 0; d < 7; d++) {
            LocalDate date = LocalDate.now().minusDays(d);
            String status = d < 5 ? "Normal" : (d % 3 == 0 ? "Late" : "Normal");
            seedAttendance(attendanceRepository, emp, date, status);
          }
        }
      }
    };
  }

  private Organization orgRepo(OrganizationRepository repo, String name) {
    return repo.findAll().stream().filter(o -> name.equals(o.getName())).findFirst().orElse(null);
  }

  private Employee seedEmp(EmployeeRepository repo, String no, String name, String phone, String email,
      LocalDate hireDate, String status, Organization dept, Organization pos, Employee manager) {
    Employee e = new Employee();
    e.setEmployeeNo(no);
    e.setName(name);
    e.setPhone(phone);
    e.setEmail(email);
    e.setHireDate(hireDate);
    e.setStatus(status);
    e.setOrgRef(dept);
    e.setPositionRef(pos);
    e.setManagerRef(manager);
    return repo.save(e);
  }

  private void seedSalary(SalaryRepository repo, Employee emp, LocalDate month) {
    Salary s = new Salary();
    s.setEmployee(emp);
    s.setSalaryMonth(String.format("%tF", month).substring(0, 7));
    s.setBaseSalary(8000.0 + emp.getId() * 2000);
    s.setBonus(1000.0 + emp.getId() * 500);
    s.setDeduction(200.0 + emp.getId() * 50);
    repo.save(s);
  }

  private void seedAttendance(AttendanceRepository repo, Employee emp, LocalDate date, String status) {
    Attendance a = new Attendance();
    a.setEmployee(emp);
    a.setWorkDate(date);
    a.setCheckIn(LocalTime.of(8, 50).plusMinutes((long)(Math.random() * 20)));
    a.setCheckOut(LocalTime.of(18, 0));
    a.setStatus(status);
    repo.save(a);
  }

  private void seedPermission(PermissionRepository repo, String role, String method, String pathPrefix) {
    if (!repo.existsByRoleAndMethodAndPathPrefix(role, method, pathPrefix)) {
      Permission p = new Permission();
      p.setRole(role);
      p.setMethod(method);
      p.setPathPrefix(pathPrefix);
      repo.save(p);
    }
  }
}
