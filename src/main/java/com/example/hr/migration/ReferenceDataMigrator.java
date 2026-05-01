package com.example.hr.migration;

import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migrate reference data labels from English to Chinese.
 * Keeps existing records but updates role/department/position/status names.
 */
@Component
@Order(0)
public class ReferenceDataMigrator implements ApplicationRunner {
  private final JdbcTemplate jdbcTemplate;

  public ReferenceDataMigrator(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    migrateRoles();
    migrateDepartments();
    migratePositions();
    migrateEmployeeStatus();
  }

  private void migrateRoles() {
    Map<String, String> map =
        Map.of(
            "ADMIN", "管理员",
            "HR", "人事",
            "FINANCE", "财务",
            "MANAGER", "经理",
            "USER", "员工");
    map.forEach(
        (from, to) -> {
          jdbcTemplate.update("UPDATE roles SET name=? WHERE name=?", to, from);
          jdbcTemplate.update("UPDATE users SET role=? WHERE role=?", to, from);
          jdbcTemplate.update("UPDATE permissions SET role=? WHERE role=?", to, from);
        });
  }

  private void migrateDepartments() {
    Map<String, String> map =
        Map.of(
            "Headquarters", "总部",
            "HR", "人力资源部",
            "Engineering", "研发部",
            "Finance", "财务部");
    map.forEach(
        (from, to) -> {
          jdbcTemplate.update("UPDATE departments SET name=? WHERE name=?", to, from);
          jdbcTemplate.update("UPDATE employees SET department=? WHERE department=?", to, from);
        });
  }

  private void migratePositions() {
    Map<String, String> map =
        Map.of(
            "HR Manager", "人力资源经理",
            "Software Engineer", "软件工程师",
            "Analyst", "分析师");
    map.forEach(
        (from, to) -> {
          jdbcTemplate.update("UPDATE positions SET name=? WHERE name=?", to, from);
          jdbcTemplate.update("UPDATE employees SET title=? WHERE title=?", to, from);
        });
  }

  private void migrateEmployeeStatus() {
    jdbcTemplate.update("UPDATE employees SET status='在职' WHERE status='Active'");
    jdbcTemplate.update("UPDATE employees SET status='离职' WHERE status='Inactive'");
  }
}
