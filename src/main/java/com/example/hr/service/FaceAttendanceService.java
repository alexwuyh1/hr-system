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
  private final OpenCvFaceService openCvFaceService;

  public FaceAttendanceService(
      EmployeeRepository employeeRepository,
      AttendanceRepository attendanceRepository,
      FaceService faceService,
      OpenCvFaceService openCvFaceService) {
    this.employeeRepository = employeeRepository;
    this.attendanceRepository = attendanceRepository;
    this.faceService = faceService;
    this.openCvFaceService = openCvFaceService;
  }

  public VerificationResult verify(Long employeeId, InputStream image) throws Exception {
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    if (employee.getAvatarPath() == null) {
      throw new IllegalArgumentException("Employee has no avatar/face data");
    }
    byte[] bytes = image.readAllBytes();
    // Prefer OpenCV verification; fallback to hash if needed.
    try {
      var result =
          openCvFaceService.verify(new java.io.ByteArrayInputStream(bytes),
              java.nio.file.Path.of(employee.getAvatarPath()));
      return new VerificationResult(
          result.matched(), (int) Math.round(result.confidence()), (int) result.threshold());
    } catch (IllegalStateException | UnsatisfiedLinkError ex) {
      if (employee.getFaceHash() == null) {
        throw ex;
      }
      String hash = faceService.computeHash(new java.io.ByteArrayInputStream(bytes));
      int distance = faceService.hammingDistance(employee.getFaceHash(), hash);
      boolean matched = distance <= DEFAULT_THRESHOLD;
      return new VerificationResult(matched, distance, DEFAULT_THRESHOLD);
    }
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
    LocalTime now = LocalTime.now();
    if (attendance.getCheckIn() == null) {
      if (attendance.getCheckOut() != null) {
        throw new IllegalArgumentException("Attendance has check-out without check-in");
      }
      attendance.setCheckIn(now);
    } else if (attendance.getCheckOut() == null) {
      if (now.isBefore(attendance.getCheckIn())) {
        throw new IllegalArgumentException("Check-out cannot be earlier than check-in");
      }
      attendance.setCheckOut(now);
    } else {
      throw new IllegalArgumentException("Attendance already has check-in and check-out today");
    }
    return attendanceRepository.save(attendance);
  }

  public record VerificationResult(boolean matched, int distance, int threshold) {}
}
