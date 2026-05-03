package com.example.hr.migration;

import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
          jdbcTemplate.update("UPDATE users SET role=? WHERE role=?", to, from);
          jdbcTemplate.update("UPDATE permissions SET role=? WHERE role=?", to, from);
        });
  }

  private void migrateEmployeeStatus() {
    jdbcTemplate.update("UPDATE employees SET status='在职' WHERE status='Active'");
    jdbcTemplate.update("UPDATE employees SET status='离职' WHERE status='Inactive'");
  }
}
