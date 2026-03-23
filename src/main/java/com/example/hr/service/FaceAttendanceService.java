package com.example.hr.service;

import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

/**
 * Face verification and attendance check-in service.
 */
@Service
public class FaceAttendanceService {
  private static final int DEFAULT_THRESHOLD = 10;

  private final EmployeeRepository employeeRepository;
  private final AttendanceRepository attendanceRepository;
  private final FaceService faceService;

  public FaceAttendanceService(
      EmployeeRepository employeeRepository,
      AttendanceRepository attendanceRepository,
      FaceService faceService) {
    this.employeeRepository = employeeRepository;
    this.attendanceRepository = attendanceRepository;
    this.faceService = faceService;
  }

  public VerificationResult verify(Long employeeId, InputStream image) throws Exception {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    if (employee.getFaceHash() == null) {
      throw new IllegalArgumentException("Employee has no avatar/face data");
    }
    String hash = faceService.computeHash(image);
    int distance = faceService.hammingDistance(employee.getFaceHash(), hash);
    boolean matched = distance <= DEFAULT_THRESHOLD;
    return new VerificationResult(matched, distance, DEFAULT_THRESHOLD);
  }

  public Attendance checkIn(Long employeeId, InputStream image) throws Exception {
    VerificationResult result = verify(employeeId, image);
    if (!result.matched()) {
      throw new IllegalArgumentException("Face verification failed");
    }
    LocalDate today = LocalDate.now();
    Attendance attendance =
        attendanceRepository
            .findByEmployeeIdAndWorkDate(employeeId, today)
            .orElseGet(() -> {
              Attendance a = new Attendance();
              a.setEmployee(
                  employeeRepository
                      .findById(employeeId)
                      .orElseThrow(() -> new IllegalArgumentException("Employee not found")));
              a.setWorkDate(today);
              a.setStatus("Normal");
              return a;
            });
    if (attendance.getCheckIn() == null) {
      attendance.setCheckIn(LocalTime.now());
    } else {
      attendance.setCheckOut(LocalTime.now());
    }
    return attendanceRepository.save(attendance);
  }

  public record VerificationResult(boolean matched, int distance, int threshold) {}
}
