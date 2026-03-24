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
  private static final double FACE_SIMILARITY_THRESHOLD = 80.0;

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
      double similarity = result.similarity();
      return new VerificationResult(
          result.matched(),
          similarity,
          "opencv-cosine",
          result.distance(),
          result.threshold());
    } catch (IllegalStateException | UnsatisfiedLinkError ex) {
      if (employee.getFaceHash() == null) {
        throw ex;
      }
      String hash = faceService.computeHash(new java.io.ByteArrayInputStream(bytes));
      int distance = faceService.hammingDistance(employee.getFaceHash(), hash);
      boolean matched = distance <= DEFAULT_THRESHOLD;
      double similarity = toHashSimilarity(distance, DEFAULT_THRESHOLD);
      return new VerificationResult(matched, similarity, "hash", distance, DEFAULT_THRESHOLD);
    }
  }

  public Attendance checkIn(Long employeeId, InputStream image) throws Exception {
    VerificationResult result = verify(employeeId, image);
    ensureSimilarity(result);
    if (!result.matched()) {
      throw new IllegalArgumentException("Face verification failed");
    }
    LocalDate today = LocalDate.now();
    attendanceRepository
        .findTopByEmployeeIdAndWorkDateAndCheckOutIsNullOrderByCheckInDesc(employeeId, today)
        .ifPresent(
            open -> {
              throw new IllegalArgumentException("Please check out before checking in again");
            });
    Attendance attendance = new Attendance();
    attendance.setEmployee(
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found")));
    attendance.setWorkDate(today);
    attendance.setStatus("Normal");
    attendance.setCheckIn(LocalTime.now());
    return attendanceRepository.save(attendance);
  }

  public Attendance checkOut(Long employeeId, InputStream image) throws Exception {
    VerificationResult result = verify(employeeId, image);
    ensureSimilarity(result);
    if (!result.matched()) {
      throw new IllegalArgumentException("Face verification failed");
    }
    LocalDate today = LocalDate.now();
    Attendance attendance =
        attendanceRepository
            .findTopByEmployeeIdAndWorkDateAndCheckOutIsNullOrderByCheckInDesc(employeeId, today)
            .orElseThrow(() -> new IllegalArgumentException("No open check-in for today"));
    LocalTime now = LocalTime.now();
    if (now.isBefore(attendance.getCheckIn())) {
      throw new IllegalArgumentException("Check-out cannot be earlier than check-in");
    }
    attendance.setCheckOut(now);
    return attendanceRepository.save(attendance);
  }

  private void ensureSimilarity(VerificationResult result) {
    if (result.similarity() < FACE_SIMILARITY_THRESHOLD) {
      throw new IllegalArgumentException(
          String.format(
              "Face similarity %.1f%% is below required %.0f%%",
              result.similarity(), FACE_SIMILARITY_THRESHOLD));
    }
  }

  private double toHashSimilarity(double distance, double threshold) {
    if (threshold <= 0) {
      return 0.0;
    }
    double ratio = 1.0 - (distance / threshold);
    double clamped = Math.max(0.0, Math.min(1.0, ratio));
    return clamped * 100.0;
  }

  public record VerificationResult(
      boolean matched, double similarity, String algorithm, double distance, double threshold) {}
}
