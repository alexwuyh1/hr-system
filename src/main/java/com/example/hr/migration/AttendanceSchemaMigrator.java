package com.example.hr.migration;

import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migrates attendance table to allow multiple records per employee per day.
 * SQLite requires table rebuild to drop UNIQUE constraints.
 */
@Component
public class AttendanceSchemaMigrator implements ApplicationRunner {
  private final JdbcTemplate jdbcTemplate;

  public AttendanceSchemaMigrator(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!attendanceTableExists()) {
      return;
    }
    if (!hasUniqueEmployeeDateIndex()) {
      return;
    }
    migrateAttendanceTable();
  }

  private boolean attendanceTableExists() {
    try {
      List<Map<String, Object>> rows =
          jdbcTemplate.queryForList(
              "SELECT name FROM sqlite_master WHERE type='table' AND name='attendance'");
      return !rows.isEmpty();
    } catch (Exception e) {
      return false;
    }
  }

  private boolean hasUniqueEmployeeDateIndex() {
    List<Map<String, Object>> rows = jdbcTemplate.queryForList("PRAGMA index_list('attendance')");
    for (Map<String, Object> row : rows) {
      Object unique = row.get("unique");
      Object origin = row.get("origin");
      if (unique != null && "1".equals(unique.toString()) && "u".equals(String.valueOf(origin))) {
        return true;
      }
    }
    return false;
  }

  private void migrateAttendanceTable() {
    jdbcTemplate.execute("PRAGMA foreign_keys=off");
    try {
      jdbcTemplate.execute("ALTER TABLE attendance RENAME TO attendance_old");
      jdbcTemplate.execute(
          "CREATE TABLE attendance ("
              + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
              + "employee_id INTEGER NOT NULL,"
              + "work_date TEXT NOT NULL,"
              + "check_in TEXT,"
              + "check_out TEXT,"
              + "status TEXT NOT NULL,"
              + "note TEXT,"
              + "late_minutes INTEGER,"
              + "overtime_minutes INTEGER,"
              + "FOREIGN KEY (employee_id) REFERENCES employees(id)"
              + ")");
      jdbcTemplate.execute(
          "INSERT INTO attendance (id, employee_id, work_date, check_in, check_out, status, note, late_minutes, overtime_minutes) "
              + "SELECT id, employee_id, work_date, check_in, check_out, status, note, late_minutes, overtime_minutes "
              + "FROM attendance_old");
      jdbcTemplate.execute("DROP TABLE attendance_old");
    } finally {
      jdbcTemplate.execute("PRAGMA foreign_keys=on");
    }
  }
}
