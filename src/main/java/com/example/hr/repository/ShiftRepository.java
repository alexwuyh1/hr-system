package com.example.hr.repository;

import com.example.hr.model.Shift;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Shift repository.
 */
public interface ShiftRepository extends JpaRepository<Shift, Long> {
  Optional<Shift> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
  List<Shift> findByWorkDate(LocalDate workDate);
}
