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
      Attendance attendance =
          attendanceRepository
              .findByEmployeeIdAndWorkDate(shift.getEmployee().getId(), date)
              .orElseGet(() -> {
                Attendance a = new Attendance();
                a.setEmployee(shift.getEmployee());
                a.setWorkDate(date);
                a.setStatus("Absent");
                return a;
              });

      boolean onLeave =
          !leaveRequestRepository.findApprovedForDate(shift.getEmployee().getId(), date).isEmpty();
      if (onLeave) {
        attendance.setStatus("Leave");
        attendance.setLateMinutes(0);
        attendance.setOvertimeMinutes(0);
        attendanceRepository.save(attendance);
        updated++;
        continue;
      }

      LocalTime checkIn = attendance.getCheckIn();
      LocalTime checkOut = attendance.getCheckOut();

      if (checkIn == null) {
        attendance.setStatus("Absent");
        attendance.setLateMinutes(0);
        attendance.setOvertimeMinutes(0);
        attendanceRepository.save(attendance);
        updated++;
        continue;
      }

      LocalTime shiftStart = shift.getStartTime();
      LocalTime shiftEnd = shift.getEndTime();

      long lateMinutes =
          Duration.between(shiftStart, checkIn).toMinutes() - rule.getLateGraceMinutes();
      if (lateMinutes > 0) {
        attendance.setStatus("Late");
        attendance.setLateMinutes((int) lateMinutes);
      } else {
        attendance.setStatus("Normal");
        attendance.setLateMinutes(0);
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

      attendance.setOvertimeMinutes(overtimeMinutes);
      attendanceRepository.save(attendance);
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
