package com.example.hr.repository;

import com.example.hr.model.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Attendance rule repository.
 */
public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {}
