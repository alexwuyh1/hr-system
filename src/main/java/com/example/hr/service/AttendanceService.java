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
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AttendanceService {
    private static final Set<String> ALLOWED_STATUS = Set.of("Normal", "Late", "Absent", "Leave");

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository, 
            EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Attendance> list() {
        return attendanceRepository.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance create(AttendanceRequest request) {
        Attendance attendance = new Attendance();
        apply(attendance, request);
        return attendanceRepository.save(attendance);
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance update(Long id, AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("考勤记录", id));
        apply(attendance, request);
        return attendanceRepository.save(attendance);
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
        
        validateStatus(request.status);
        validateTimes(request.checkIn, request.checkOut);
        
        attendance.setEmployee(employee);
        attendance.setWorkDate(request.workDate);
        attendance.setCheckIn(request.checkIn);
        attendance.setCheckOut(request.checkOut);
        attendance.setStatus(request.status);
        attendance.setNote(request.note);
    }

    private void validateStatus(String status) {
        if (!ALLOWED_STATUS.contains(status)) {
            throw new InvalidParameterException("status", "状态必须是：Normal, Late, Absent, Leave 之一");
        }
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
