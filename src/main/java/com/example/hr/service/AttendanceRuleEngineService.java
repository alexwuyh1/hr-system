package com.example.hr.service;

import com.example.hr.model.Attendance;
import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Shift;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.LeaveRequestRepository;
import com.example.hr.repository.OvertimeRequestRepository;
import com.example.hr.repository.ShiftRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AttendanceRuleEngineService {
  private final AttendanceRepository attendanceRepository;
  private final ShiftRepository shiftRepository;
  private final LeaveRequestRepository leaveRequestRepository;
  private final OvertimeRequestRepository overtimeRequestRepository;
  private final AttendanceRuleService attendanceRuleService;
  private final EmployeeRepository employeeRepository;

  public AttendanceRuleEngineService(
      AttendanceRepository attendanceRepository,
      ShiftRepository shiftRepository,
      LeaveRequestRepository leaveRequestRepository,
      OvertimeRequestRepository overtimeRequestRepository,
      AttendanceRuleService attendanceRuleService,
      EmployeeRepository employeeRepository) {
    this.attendanceRepository = attendanceRepository;
    this.shiftRepository = shiftRepository;
    this.leaveRequestRepository = leaveRequestRepository;
    this.overtimeRequestRepository = overtimeRequestRepository;
    this.attendanceRuleService = attendanceRuleService;
    this.employeeRepository = employeeRepository;
  }

  public int computeForDate(LocalDate date) {
    AttendanceRule rule = attendanceRuleService.getRule();
    LocalTime defaultStart = LocalTime.parse(rule.getWorkStartTime());
    LocalTime defaultEnd = LocalTime.parse(rule.getWorkEndTime());

    List<Shift> shifts = shiftRepository.findByWorkDate(date);
    int updated = 0;

    if (shifts.isEmpty()) {
      List<Attendance> allRecords = attendanceRepository.findByWorkDate(date);
      for (Attendance record : allRecords) {
        int result = computeSingle(record, defaultStart, defaultEnd, rule);
        if (result > 0) {
          updated++;
        }
      }
      return updated;
    }

    for (Shift shift : shifts) {
      LocalTime shiftStart = shift.getStartTime() != null ? shift.getStartTime() : defaultStart;
      LocalTime shiftEnd = shift.getEndTime() != null ? shift.getEndTime() : defaultEnd;

      List<Attendance> records =
          attendanceRepository.findByEmployeeIdAndWorkDate(shift.getEmployee().getId(), date);
      if (records.isEmpty()) {
        Attendance a = new Attendance();
        a.setEmployee(shift.getEmployee());
        a.setWorkDate(date);
        a.setStatus("Absent");
        records = List.of(a);
      }

      boolean onLeave =
          !leaveRequestRepository.findApprovedForDate(shift.getEmployee().getId(), date).isEmpty();
      if (onLeave) {
        for (Attendance entry : records) {
          entry.setStatus("Leave");
          entry.setLateMinutes(0);
          entry.setOvertimeMinutes(0);
          attendanceRepository.save(entry);
        }
        updated++;
        continue;
      }

      for (Attendance record : records) {
        int result = computeSingle(record, shiftStart, shiftEnd, rule);
        if (result > 0) {
          updated++;
        }
      }
    }

    return updated;
  }

  private int computeSingle(Attendance record, LocalTime shiftStart, LocalTime shiftEnd, AttendanceRule rule) {
    LocalTime checkIn = record.getCheckIn();
    LocalTime checkOut = record.getCheckOut();

    if (checkIn == null && checkOut == null) {
      if (!"Leave".equals(record.getStatus())) {
        record.setStatus("Absent");
        record.setLateMinutes(0);
        record.setOvertimeMinutes(0);
        attendanceRepository.save(record);
        return 1;
      }
      return 0;
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
      long earlyLeaveMinutes = Duration.between(checkOut, shiftEnd).toMinutes() - 
          (rule.getEarlyLeaveGraceMinutes() != null ? rule.getEarlyLeaveGraceMinutes() : 10);
      
      if (earlyLeaveMinutes >= absentThreshold) {
        record.setStatus("Absent");
        record.setLateMinutes(0);
      } else if (earlyLeaveMinutes > 0 && "Normal".equals(record.getStatus())) {
        record.setStatus("Early Leave");
      }

      long extra = Duration.between(shiftEnd, checkOut).toMinutes() - rule.getOvertimeThresholdMinutes();
      if (extra > 0) {
        overtimeMinutes = (int) extra;
      }
    }

    if (overtimeMinutes > 0 && Boolean.TRUE.equals(rule.getRequireOvertimeApproval())) {
      boolean approvedOvertime =
          !overtimeRequestRepository
              .findApprovedForDate(record.getEmployee().getId(), record.getWorkDate())
              .isEmpty();
      if (!approvedOvertime) {
        overtimeMinutes = 0;
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
