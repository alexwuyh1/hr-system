package com.example.hr.controller;

import com.example.hr.dto.AttendanceRuleRequest;
import com.example.hr.model.AttendanceRule;
import com.example.hr.service.AttendanceRuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Attendance rules endpoints.
 */
@RestController
@RequestMapping("/api/attendance-rules")
public class AttendanceRuleController {
  private final AttendanceRuleService attendanceRuleService;

  public AttendanceRuleController(AttendanceRuleService attendanceRuleService) {
    this.attendanceRuleService = attendanceRuleService;
  }

  @GetMapping
  public AttendanceRule getRule() {
    return attendanceRuleService.getRule();
  }

  @PutMapping
  public AttendanceRule update(@Valid @RequestBody AttendanceRuleRequest request) {
    return attendanceRuleService.update(request);
  }
}
