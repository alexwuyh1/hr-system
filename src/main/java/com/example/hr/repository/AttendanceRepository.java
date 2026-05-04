package com.example.hr.repository;

import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

/**
 * Attendance repository with date-based queries.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  long countByWorkDate(LocalDate workDate);
  List<Attendance> findByWorkDate(LocalDate workDate);
  List<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

  Optional<Attendance> findTopByEmployeeIdAndWorkDateAndCheckOutIsNullOrderByCheckInDesc(
      Long employeeId, LocalDate workDate);

  @Modifying
  void deleteByEmployee(Employee employee);
}
