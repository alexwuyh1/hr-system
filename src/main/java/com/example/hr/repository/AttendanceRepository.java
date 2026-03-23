package com.example.hr.repository;

import com.example.hr.model.Attendance;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Attendance repository with date-based queries.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  long countByWorkDate(LocalDate workDate);
  Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}
