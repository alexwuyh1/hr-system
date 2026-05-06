package com.example.hr.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AttendanceStatusInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbc;

    public AttendanceStatusInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        jdbc.update("UPDATE attendance SET status = '正常' WHERE status = 'Normal' OR status = '正常'");
        jdbc.update("UPDATE attendance SET status = '迟到' WHERE status = 'Late'");
        jdbc.update("UPDATE attendance SET status = '早退' WHERE status = 'EarlyLeave'");
        System.out.println("Attendance status updated to Chinese");
    }
}
