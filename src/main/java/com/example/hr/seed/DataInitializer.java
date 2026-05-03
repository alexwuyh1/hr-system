package com.example.hr.seed;

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
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed initial data for a fresh database.
 * 数据初始化：仅在新数据库时执行一次
 */
@Configuration
public class DataInitializer {
  @Bean
  @Order(1)
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
        admin.setRole("管理员");
        admin.setCreatedAt(System.currentTimeMillis());
        userRepository.save(admin);
      }

      if (roleRepository.count() == 0) {
        for (String roleName : new String[] {"管理员", "人事", "员工"}) {
          Role role = new Role();
          role.setName(roleName);
          roleRepository.save(role);
        }
      }

      // 管理员：所有API权限
      String[] adminRoles = {"管理员"};

      // 人事：除权限管理外的所有权限
      String[] hrRoles = {"管理员", "人事"};

      // 员工：仅人脸打卡
      String[] employeeRoles = {"管理员", "人事", "员工"};

      // 员工管理
      seedPermission(permissionRepository, "GET", "/api/employees", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/employees", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/employees", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/employees", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/employees/resign", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/employees/rehire", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/employees/{id}/avatar", hrRoles);
      seedPermission(permissionRepository, "GET", "/api/employees/{id}/avatar", hrRoles);

      // 考勤管理
      seedPermission(permissionRepository, "GET", "/api/attendance", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/attendance", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/attendance", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/attendance", hrRoles);

      // 考勤规则
      seedPermission(permissionRepository, "GET", "/api/attendance-rules", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/attendance-rules", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/attendance-rules", hrRoles);

      // 薪资管理
      seedPermission(permissionRepository, "GET", "/api/salaries", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/salaries", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/salaries", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/salaries", hrRoles);

      // 部门管理
      seedPermission(permissionRepository, "GET", "/api/departments", hrRoles);
      seedPermission(permissionRepository, "GET", "/api/departments/tree", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/departments", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/departments", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/departments", hrRoles);

      // 职位管理
      seedPermission(permissionRepository, "GET", "/api/positions", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/positions", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/positions", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/positions", hrRoles);

      // 职级管理
      seedPermission(permissionRepository, "GET", "/api/grades", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/grades", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/grades", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/grades", hrRoles);

      // 排班管理
      seedPermission(permissionRepository, "GET", "/api/shifts", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/shifts", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/shifts", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/shifts", hrRoles);

      // 请假管理
      seedPermission(permissionRepository, "GET", "/api/leaves", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/leaves", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/leaves", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/leaves", hrRoles);

      // 加班管理
      seedPermission(permissionRepository, "GET", "/api/overtime", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/overtime", hrRoles);
      seedPermission(permissionRepository, "PUT", "/api/overtime", hrRoles);
      seedPermission(permissionRepository, "DELETE", "/api/overtime", hrRoles);

      // 人脸打卡
      seedPermission(permissionRepository, "POST", "/api/face/verify", employeeRoles);
      seedPermission(permissionRepository, "POST", "/api/face/checkin", employeeRoles);
      seedPermission(permissionRepository, "POST", "/api/face/checkout", employeeRoles);

      // 导入导出
      seedPermission(permissionRepository, "GET", "/api/data", hrRoles);
      seedPermission(permissionRepository, "POST", "/api/data", hrRoles);

      // 仪表盘
      seedPermission(permissionRepository, "GET", "/api/dashboard", hrRoles);

      // 报表
      seedPermission(permissionRepository, "GET", "/api/reports", hrRoles);

      // 初始化
      seedPermission(permissionRepository, "GET", "/api/init", hrRoles);

      // 用户管理（仅管理员）
      seedPermission(permissionRepository, "GET", "/api/users", adminRoles);
      seedPermission(permissionRepository, "PUT", "/api/users", adminRoles);

      // 权限管理（仅管理员）
      seedPermission(permissionRepository, "GET", "/api/permissions", adminRoles);
      seedPermission(permissionRepository, "POST", "/api/permissions", adminRoles);
      seedPermission(permissionRepository, "PUT", "/api/permissions", adminRoles);
      seedPermission(permissionRepository, "DELETE", "/api/permissions", adminRoles);
      seedPermission(permissionRepository, "GET", "/api/roles", adminRoles);
      seedPermission(permissionRepository, "POST", "/api/roles", adminRoles);
      seedPermission(permissionRepository, "DELETE", "/api/roles", adminRoles);

      if (departmentRepository.count() == 0) {
        Department hq = new Department();
        hq.setName("总部");
        departmentRepository.save(hq);

        Department hr = new Department();
        hr.setName("人力资源部");
        hr.setParent(hq);
        departmentRepository.save(hr);

        Department eng = new Department();
        eng.setName("研发部");
        eng.setParent(hq);
        departmentRepository.save(eng);

        Department fin = new Department();
        fin.setName("财务部");
        fin.setParent(hq);
        departmentRepository.save(fin);
      }

      if (positionRepository.count() == 0) {
        Position p1 = new Position();
        p1.setName("人力资源经理");
        positionRepository.save(p1);

        Position p2 = new Position();
        p2.setName("软件工程师");
        positionRepository.save(p2);

        Position p3 = new Position();
        p3.setName("分析师");
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
        Department hr = departmentRepository.findAll().stream().filter(d -> "人力资源部".equals(d.getName())).findFirst().orElse(null);
        Department eng = departmentRepository.findAll().stream().filter(d -> "研发部".equals(d.getName())).findFirst().orElse(null);
        Position hrMgr = positionRepository.findAll().stream().filter(p -> "人力资源经理".equals(p.getName())).findFirst().orElse(null);
        Position engPos = positionRepository.findAll().stream().filter(p -> "软件工程师".equals(p.getName())).findFirst().orElse(null);
        Grade g4 = gradeRepository.findAll().stream().filter(g -> g.getLevel() == 4).findFirst().orElse(null);
        Grade g3 = gradeRepository.findAll().stream().filter(g -> g.getLevel() == 3).findFirst().orElse(null);

        Employee e1 = new Employee();
        e1.setEmployeeNo("E1001");
        e1.setName("Alice Chen");
        e1.setDepartment("人力资源部");
        e1.setTitle("人力资源经理");
        e1.setPhone("13800000001");
        e1.setEmail("alice@example.com");
        e1.setHireDate(LocalDate.now().minusYears(2));
        e1.setStatus("在职");
        e1.setDepartmentRef(hr);
        e1.setPositionRef(hrMgr);
        e1.setGradeRef(g4);
        employeeRepository.save(e1);

        Employee e2 = new Employee();
        e2.setEmployeeNo("E1002");
        e2.setName("Bob Li");
        e2.setDepartment("研发部");
        e2.setTitle("软件工程师");
        e2.setPhone("13800000002");
        e2.setEmail("bob@example.com");
        e2.setHireDate(LocalDate.now().minusYears(1));
        e2.setStatus("在职");
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
