package com.example.hr.service;

import com.example.hr.dto.AttendanceRequest;
import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Attendance service for CRUD and validation.
 */
@Service
public class AttendanceService {
  private static final Set<String> ALLOWED_STATUS =
      Set.of("Normal", "Late", "Absent", "Leave");

  private final AttendanceRepository attendanceRepository;
  private final EmployeeRepository employeeRepository;

  public AttendanceService(
      AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
    this.attendanceRepository = attendanceRepository;
    this.employeeRepository = employeeRepository;
  }

  public List<Attendance> list() {
    return attendanceRepository.findAll();
  }

  public Attendance create(AttendanceRequest request) {
    Attendance attendance = new Attendance();
    apply(attendance, request);
    return attendanceRepository.save(attendance);
  }

  public Attendance update(Long id, AttendanceRequest request) {
    Attendance attendance =
        attendanceRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Attendance not found"));
    apply(attendance, request);
    return attendanceRepository.save(attendance);
  }

  public void delete(Long id) {
    attendanceRepository.deleteById(id);
  }

  private void apply(Attendance attendance, AttendanceRequest request) {
    // Ensure employee exists to keep FK integrity.
    Employee employee =
        employeeRepository
            .findById(request.employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    ensureUnique(attendance, request.employeeId, request.workDate);
    validateStatus(request.status);
    validateTimes(request.checkIn, request.checkOut);
    attendance.setEmployee(employee);
    attendance.setWorkDate(request.workDate);
    attendance.setCheckIn(request.checkIn);
    attendance.setCheckOut(request.checkOut);
    attendance.setStatus(request.status);
    attendance.setNote(request.note);
  }

  private void ensureUnique(Attendance attendance, Long employeeId, java.time.LocalDate workDate) {
    attendanceRepository
        .findByEmployeeIdAndWorkDate(employeeId, workDate)
        .ifPresent(
            existing -> {
              if (attendance.getId() == null || !existing.getId().equals(attendance.getId())) {
                throw new IllegalArgumentException("Attendance already exists for this employee/date");
              }
            });
  }

  private void validateStatus(String status) {
    if (!ALLOWED_STATUS.contains(status)) {
      throw new IllegalArgumentException("Invalid attendance status");
    }
  }

  private void validateTimes(LocalTime checkIn, LocalTime checkOut) {
    if (checkOut != null && checkIn == null) {
      throw new IllegalArgumentException("Check-out requires a check-in time");
    }
    if (checkIn != null && checkOut != null && checkOut.isBefore(checkIn)) {
      throw new IllegalArgumentException("Check-out cannot be earlier than check-in");
    }
  }
}
