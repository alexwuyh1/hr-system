package com.example.hr.service;

import com.example.hr.dto.AttendanceRequest;
import com.example.hr.exception.BadRequestException;
import com.example.hr.exception.NotFoundException;
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
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        attendance.setEmployee(employee);
        attendance.setWorkDate(today);
        attendance.setCheckIn(now);
        attendance.setCheckOut(null);
        attendance.setStatus("Normal");
        attendance.setNote(request.note);
    }
}
