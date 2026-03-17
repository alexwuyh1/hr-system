package com.example.hr.controller;

import com.example.hr.dto.AttendanceRequest;
import com.example.hr.model.Attendance;
import com.example.hr.service.AttendanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Attendance CRUD endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
  private final AttendanceService attendanceService;

  public AttendanceController(AttendanceService attendanceService) {
    this.attendanceService = attendanceService;
  }

  @GetMapping
  public List<Attendance> list() {
    return attendanceService.list();
  }

  @PostMapping
  public Attendance create(@Valid @RequestBody AttendanceRequest request) {
    return attendanceService.create(request);
  }

  @PutMapping("/{id}")
  public Attendance update(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
    return attendanceService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    attendanceService.delete(id);
  }
}
