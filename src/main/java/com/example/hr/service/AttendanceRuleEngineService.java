package com.example.hr.service;

import com.example.hr.model.Attendance;
import com.example.hr.model.AttendanceRule;
import com.example.hr.repository.AttendanceRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AttendanceRuleEngineService {
  private final AttendanceRepository attendanceRepository;
  private final AttendanceRuleService attendanceRuleService;

  public AttendanceRuleEngineService(
      AttendanceRepository attendanceRepository,
      AttendanceRuleService attendanceRuleService) {
    this.attendanceRepository = attendanceRepository;
    this.attendanceRuleService = attendanceRuleService;
  }

  public int computeForDate(LocalDate date) {
    AttendanceRule rule = attendanceRuleService.getRule();
    LocalTime defaultStart = LocalTime.parse(rule.getWorkStartTime());
    LocalTime defaultEnd = LocalTime.parse(rule.getWorkEndTime());

    List<Attendance> allRecords = attendanceRepository.findByWorkDate(date);
    int updated = 0;

    for (Attendance record : allRecords) {
      int result = computeSingle(record, defaultStart, defaultEnd, rule);
      if (result > 0) {
        updated++;
      }
    }
    return updated;
  }

  private int computeSingle(Attendance record, LocalTime shiftStart, LocalTime shiftEnd, AttendanceRule rule) {
    LocalTime checkIn = record.getCheckIn();
    LocalTime checkOut = record.getCheckOut();

    if (checkIn == null && checkOut == null) {
      record.setStatus("Absent");
      record.setLateMinutes(0);
      record.setOvertimeMinutes(0);
      attendanceRepository.save(record);
      return 1;
    }

    if (checkIn == null) {
      record.setStatus("Absent");
      record.setLateMinutes(0);
      record.setOvertimeMinutes(0);
      attendanceRepository.save(record);
      return 1;
    }

    long lateMinutes = Duration.between(shiftStart, checkIn).toMinutes() - rule.getLateGraceMinutes();
    int absentThreshold = rule.getAbsentThresholdMinutes() != null ? rule.getAbsentThresholdMinutes() : 240;

    if (lateMinutes >= absentThreshold) {
      record.setStatus("Absent");
      record.setLateMinutes(0);
    } else if (lateMinutes > 0) {
      record.setStatus("Late");
      record.setLateMinutes((int) lateMinutes);
    } else {
      record.setStatus("Normal");
      record.setLateMinutes(0);
    }

    int overtimeMinutes = 0;
    if (checkOut != null) {
      long extra = Duration.between(shiftEnd, checkOut).toMinutes() - rule.getOvertimeThresholdMinutes();
      if (extra > 0) {
        overtimeMinutes = (int) extra;
      }
    }

    record.setOvertimeMinutes(overtimeMinutes);
    attendanceRepository.save(record);
    return 1;
  }

  public int computeRange(LocalDate start, LocalDate end) {
    int total = 0;
    LocalDate d = start;
    while (!d.isAfter(end)) {
      total += computeForDate(d);
      d = d.plusDays(1);
    }
    return total;
  }
}
