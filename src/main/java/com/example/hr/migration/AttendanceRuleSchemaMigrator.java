package com.example.hr.migration;

import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AttendanceRuleSchemaMigrator implements ApplicationRunner {
  private final JdbcTemplate jdbcTemplate;

  public AttendanceRuleSchemaMigrator(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!attendanceRuleTableExists()) {
      return;
    }
    addColumnIfNotExists("attendance_rules", "work_start_time", "TEXT");
    addColumnIfNotExists("attendance_rules", "work_end_time", "TEXT");
    addColumnIfNotExists("attendance_rules", "late_grace_minutes", "INTEGER");
    addColumnIfNotExists("attendance_rules", "absent_threshold_minutes", "INTEGER");
    addColumnIfNotExists("attendance_rules", "overtime_threshold_minutes", "INTEGER");
  }

  private boolean attendanceRuleTableExists() {
    List<Map<String, Object>> rows =
        jdbcTemplate.queryForList(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='attendance_rules'");
    return !rows.isEmpty();
  }

  private void addColumnIfNotExists(String table, String column, String type) {
    if (!columnExists(table, column)) {
      jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }
  }

  private boolean columnExists(String table, String column) {
    List<Map<String, Object>> rows = jdbcTemplate.queryForList("PRAGMA table_info(" + table + ")");
    for (Map<String, Object> row : rows) {
      if (column.equals(String.valueOf(row.get("name")))) {
        return true;
      }
    }
    return false;
  }
}
