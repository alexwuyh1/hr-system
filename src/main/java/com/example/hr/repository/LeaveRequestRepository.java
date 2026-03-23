package com.example.hr.repository;

import com.example.hr.model.LeaveRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Leave request repository.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
  @Query(
      "select l from LeaveRequest l where l.employee.id = ?1 and l.status = 'APPROVED' and ?2 between l.startDate and l.endDate")
  List<LeaveRequest> findApprovedForDate(Long employeeId, LocalDate date);
}
