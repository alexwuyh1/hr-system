package com.example.hr.service;

import com.example.hr.dto.AttendanceRequest;
import com.example.hr.exception.EmployeeNotActiveException;
import com.example.hr.exception.EmployeeNotFoundException;
import com.example.hr.exception.InvalidParameterException;
import com.example.hr.exception.InvalidStateException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRuleEngineService attendanceRuleEngineService;

    public AttendanceService(
            AttendanceRepository attendanceRepository, 
            EmployeeRepository employeeRepository,
            AttendanceRuleEngineService attendanceRuleEngineService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRuleEngineService = attendanceRuleEngineService;
    }

    public List<Attendance> list() {
        return attendanceRepository.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance create(AttendanceRequest request) {
        Attendance attendance = new Attendance();
        apply(attendance, request);
        attendance = attendanceRepository.save(attendance);
        attendanceRuleEngineService.computeSingle(attendance);
        return attendance;
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance update(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("考勤记录", id));
        apply(attendance, request);
        attendance = attendanceRepository.save(attendance);
        attendanceRuleEngineService.computeSingle(attendance);
        return attendance;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }

    private void apply(Attendance attendance, AttendanceRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(request.employeeId));
        
        if (!"在职".equals(employee.getStatus())) {
            throw new EmployeeNotActiveException(request.employeeId);
        }
        
        validateTimes(request.checkIn, request.checkOut);
        
        attendance.setEmployee(employee);
        attendance.setWorkDate(request.workDate);
        attendance.setCheckIn(request.checkIn);
        attendance.setCheckOut(request.checkOut);
        attendance.setNote(request.note);
    }

    private void validateTimes(LocalTime checkIn, LocalTime checkOut) {
        if (checkOut != null && checkIn == null) {
            throw new InvalidParameterException("checkIn", "签退需要先填写签到时间");
        }
        if (checkIn != null && checkOut != null && checkOut.isBefore(checkIn)) {
            throw new InvalidParameterException("checkOut", "签退时间不能早于签到时间");
        }
    }
}
