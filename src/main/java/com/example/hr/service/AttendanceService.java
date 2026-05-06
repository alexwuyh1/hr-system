package com.example.hr.service;

import com.example.hr.dto.AttendanceRequest;
import com.example.hr.exception.BadRequestException;
import com.example.hr.exception.NotFoundException;
import com.example.hr.model.Attendance;
import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Employee;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRuleService attendanceRuleService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            AttendanceRuleService attendanceRuleService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRuleService = attendanceRuleService;
    }

    public List<Attendance> list() {
        List<Attendance> list = attendanceRepository.findAll();
        list.forEach(a -> initEmployee(a.getEmployee()));
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance create(AttendanceRequest request) {
        Attendance attendance = new Attendance();
        apply(attendance, request);
        attendance = attendanceRepository.save(attendance);
        initEmployee(attendance.getEmployee());
        return attendance;
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance update(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("考勤记录", id));
        apply(attendance, request);
        attendance = attendanceRepository.save(attendance);
        initEmployee(attendance.getEmployee());
        return attendance;
    }

    private void initEmployee(Employee employee) {
        if (employee == null) return;
        employee.getName();
        if (employee.getOrgRef() != null) employee.getOrgRef().getName();
        if (employee.getPositionRef() != null) employee.getPositionRef().getName();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }

    private void apply(Attendance attendance, AttendanceRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId)
            .orElseThrow(() -> new NotFoundException("员工", request.employeeId));

        if (!"在职".equals(employee.getStatus())) {
            throw new BadRequestException("员工未在职，ID: " + request.employeeId);
        }

        AttendanceRule rule = attendanceRuleService.getRule();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        attendance.setEmployee(employee);
        attendance.setWorkDate(today);
        attendance.setCheckIn(now);
        attendance.setCheckOut(null);
        attendance.setNote(request.note);

        calculateAttendanceStatus(attendance, rule, now, null);
    }

    private void calculateAttendanceStatus(Attendance attendance, AttendanceRule rule, LocalTime checkIn, LocalTime checkOut) {
        LocalTime workStart = LocalTime.parse(rule.getWorkStartTime());
        LocalTime workEnd = LocalTime.parse(rule.getWorkEndTime());
        int lateGrace = rule.getLateGraceMinutes() != null ? rule.getLateGraceMinutes() : 10;
        int absentThreshold = rule.getAbsentThresholdMinutes() != null ? rule.getAbsentThresholdMinutes() : 240;
        int earlyLeaveGrace = rule.getEarlyLeaveGraceMinutes() != null ? rule.getEarlyLeaveGraceMinutes() : 10;

        LocalTime lateGraceTime = workStart.plusMinutes(lateGrace);
        LocalTime absentTime = workStart.plusMinutes(absentThreshold);
        LocalTime earlyLeaveTime = workEnd.minusMinutes(earlyLeaveGrace);

        StringBuilder statusBuilder = new StringBuilder();
        int lateMinutes = 0;
        int absentMinutes = 0;
        int earlyLeaveMinutes = 0;

        if (checkIn != null) {
            if (checkIn.isAfter(absentTime)) {
                absentMinutes = (int) Duration.between(workStart, checkIn).toMinutes();
                statusBuilder.append("旷工");
            } else if (checkIn.isAfter(lateGraceTime)) {
                lateMinutes = (int) Duration.between(lateGraceTime, checkIn).toMinutes();
                if (statusBuilder.length() > 0) statusBuilder.append("+");
                statusBuilder.append("迟到");
            }
        }

        if (checkOut != null) {
            if (checkOut.isBefore(earlyLeaveTime)) {
                earlyLeaveMinutes = (int) Duration.between(checkOut, earlyLeaveTime).toMinutes();
                if (statusBuilder.length() > 0) statusBuilder.append("+");
                statusBuilder.append("早退");
            }
        }

        if (statusBuilder.length() == 0) {
            statusBuilder.append("正常");
        }

        attendance.setStatus(statusBuilder.toString());
        attendance.setLateMinutes(lateMinutes > 0 ? lateMinutes : null);
        attendance.setAbsentMinutes(absentMinutes > 0 ? absentMinutes : null);
        attendance.setEarlyLeaveMinutes(earlyLeaveMinutes > 0 ? earlyLeaveMinutes : null);
    }
}