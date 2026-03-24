package com.example.hr.controller;

import com.example.hr.model.Attendance;
import com.example.hr.service.FaceAttendanceService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Face verification and attendance endpoints (1:1).
 */
@RestController
@RequestMapping("/api/face")
public class FaceController {
  private final FaceAttendanceService faceAttendanceService;

  public FaceController(FaceAttendanceService faceAttendanceService) {
    this.faceAttendanceService = faceAttendanceService;
  }

  @PostMapping("/verify")
  public Map<String, Object> verify(
      @RequestParam("employeeId") Long employeeId, @RequestParam("file") MultipartFile file)
      throws Exception {
    var result = faceAttendanceService.verify(employeeId, file.getInputStream());
    return Map.of(
        "matched", result.matched(),
        "similarity", result.similarity(),
        "algorithm", result.algorithm(),
        "distance", result.distance(),
        "threshold", result.threshold());
  }

  @PostMapping("/checkin")
  public Attendance checkIn(
      @RequestParam("employeeId") Long employeeId, @RequestParam("file") MultipartFile file)
      throws Exception {
    return faceAttendanceService.checkIn(employeeId, file.getInputStream());
  }

  @PostMapping("/checkout")
  public Attendance checkOut(
      @RequestParam("employeeId") Long employeeId, @RequestParam("file") MultipartFile file)
      throws Exception {
    return faceAttendanceService.checkOut(employeeId, file.getInputStream());
  }
}
