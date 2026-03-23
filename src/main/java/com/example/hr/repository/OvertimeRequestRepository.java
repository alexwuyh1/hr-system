package com.example.hr.repository;

import com.example.hr.model.OvertimeRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Overtime request repository.
 */
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
  @Query(
      "select o from OvertimeRequest o where o.employee.id = ?1 and o.status = 'APPROVED' and o.workDate = ?2")
  List<OvertimeRequest> findApprovedForDate(Long employeeId, LocalDate date);
}
