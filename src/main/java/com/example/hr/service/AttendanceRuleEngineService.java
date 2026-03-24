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

/**
 * Attendance rule engine to compute status, late minutes, and overtime.
 */
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
    List<Shift> shifts = shiftRepository.findByWorkDate(date);
    int updated = 0;

    for (Shift shift : shifts) {
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

      LocalTime checkIn =
          records.stream()
              .map(Attendance::getCheckIn)
              .filter(Objects::nonNull)
              .min(LocalTime::compareTo)
              .orElse(null);
      LocalTime checkOut =
          records.stream()
              .map(Attendance::getCheckOut)
              .filter(Objects::nonNull)
              .max(LocalTime::compareTo)
              .orElse(null);

      if (checkIn == null) {
        for (Attendance entry : records) {
          entry.setStatus("Absent");
          entry.setLateMinutes(0);
          entry.setOvertimeMinutes(0);
          attendanceRepository.save(entry);
        }
        updated++;
        continue;
      }

      LocalTime shiftStart = shift.getStartTime();
      LocalTime shiftEnd = shift.getEndTime();

      long lateMinutes =
          Duration.between(shiftStart, checkIn).toMinutes() - rule.getLateGraceMinutes();
      if (lateMinutes > 0) {
        for (Attendance entry : records) {
          entry.setStatus("Late");
          entry.setLateMinutes((int) lateMinutes);
        }
      } else {
        for (Attendance entry : records) {
          entry.setStatus("Normal");
          entry.setLateMinutes(0);
        }
      }

      int overtimeMinutes = 0;
      if (checkOut != null) {
        long extra =
            Duration.between(shiftEnd, checkOut).toMinutes() - rule.getOvertimeThresholdMinutes();
        if (extra > 0) {
          overtimeMinutes = (int) extra;
        }
      }

      // Require approved overtime request if any overtime exists
      if (overtimeMinutes > 0) {
        boolean approvedOvertime =
            !overtimeRequestRepository
                .findApprovedForDate(shift.getEmployee().getId(), date)
                .isEmpty();
        if (!approvedOvertime) {
          overtimeMinutes = 0;
        }
      }

      for (Attendance entry : records) {
        entry.setOvertimeMinutes(overtimeMinutes);
        attendanceRepository.save(entry);
      }
      updated++;
    }

    // If no shift, do nothing. This keeps the rule engine focused on schedule days.
    return updated;
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
