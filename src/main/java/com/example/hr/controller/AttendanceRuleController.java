package com.example.hr.controller;

import com.example.hr.dto.AttendanceRuleRequest;
import com.example.hr.model.AttendanceRule;
import com.example.hr.service.AttendanceRuleEngineService;
import com.example.hr.service.AttendanceRuleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * Attendance rules and calculation endpoints.
 */
@RestController
@RequestMapping("/api/attendance-rules")
public class AttendanceRuleController {
  private final AttendanceRuleService attendanceRuleService;
  private final AttendanceRuleEngineService attendanceRuleEngineService;

  public AttendanceRuleController(
      AttendanceRuleService attendanceRuleService,
      AttendanceRuleEngineService attendanceRuleEngineService) {
    this.attendanceRuleService = attendanceRuleService;
    this.attendanceRuleEngineService = attendanceRuleEngineService;
  }

  @GetMapping
  public AttendanceRule getRule() {
    return attendanceRuleService.getRule();
  }

  @PutMapping
  public AttendanceRule update(@Valid @RequestBody AttendanceRuleRequest request) {
    return attendanceRuleService.update(request);
  }

  @PostMapping("/calculate")
  public Map<String, Integer> calculate(@RequestParam("date") String date) {
    int updated = attendanceRuleEngineService.computeForDate(LocalDate.parse(date));
    return Map.of("updated", updated);
  }

  @PostMapping("/calculate-range")
  public Map<String, Integer> calculateRange(
      @RequestParam("start") String start, @RequestParam("end") String end) {
    int updated =
        attendanceRuleEngineService.computeRange(LocalDate.parse(start), LocalDate.parse(end));
    return Map.of("updated", updated);
  }
}
