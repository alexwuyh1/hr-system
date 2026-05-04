package com.example.hr.service;

import com.example.hr.dto.AttendanceRequest;
import com.example.hr.exception.EmployeeNotActiveException;
import com.example.hr.exception.EmployeeNotFoundException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AttendanceService {
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
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        attendance.setEmployee(employee);
        attendance.setWorkDate(today);
        attendance.setCheckIn(now);
        attendance.setCheckOut(null);
        attendance.setNote(request.note);
    }
}
