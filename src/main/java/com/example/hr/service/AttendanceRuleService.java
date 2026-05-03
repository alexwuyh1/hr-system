package com.example.hr.service;

import com.example.hr.dto.AttendanceRuleRequest;
import com.example.hr.model.AttendanceRule;
import com.example.hr.repository.AttendanceRuleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AttendanceRuleService {
  private final AttendanceRuleRepository attendanceRuleRepository;

  public AttendanceRuleService(AttendanceRuleRepository attendanceRuleRepository) {
    this.attendanceRuleRepository = attendanceRuleRepository;
  }

  public AttendanceRule getRule() {
    List<AttendanceRule> rules = attendanceRuleRepository.findAll();
    if (rules.isEmpty()) {
      AttendanceRule rule = new AttendanceRule();
      rule.setWorkStartTime("09:00");
      rule.setWorkEndTime("18:00");
      rule.setLateGraceMinutes(10);
      rule.setAbsentThresholdMinutes(240);
      rule.setOvertimeThresholdMinutes(30);
      return attendanceRuleRepository.save(rule);
    }
    return rules.get(0);
  }

  public AttendanceRule update(AttendanceRuleRequest request) {
    AttendanceRule rule = getRule();
    rule.setWorkStartTime(request.workStartTime);
    rule.setWorkEndTime(request.workEndTime);
    rule.setLateGraceMinutes(request.lateGraceMinutes);
    rule.setAbsentThresholdMinutes(request.absentThresholdMinutes);
    rule.setOvertimeThresholdMinutes(request.overtimeThresholdMinutes);
    return attendanceRuleRepository.save(rule);
  }
}
