package com.example.hr.service;

import com.example.hr.dto.ReportResponse;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.SalaryRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Report service builds simple aggregated KPIs.
 */
@Service
public class ReportService {
  private final EmployeeRepository employeeRepository;
  private final AttendanceRepository attendanceRepository;
  private final SalaryRepository salaryRepository;

  public ReportService(
      EmployeeRepository employeeRepository,
      AttendanceRepository attendanceRepository,
      SalaryRepository salaryRepository) {
    this.employeeRepository = employeeRepository;
    this.attendanceRepository = attendanceRepository;
    this.salaryRepository = salaryRepository;
  }

  public ReportResponse summary() {
    ReportResponse response = new ReportResponse();
    response.totalEmployees = employeeRepository.count();
    response.activeEmployees = employeeRepository.count(); // Simple demo: all are active
    response.attendanceToday = attendanceRepository.countByWorkDate(LocalDate.now());
    response.totalPayroll = salaryRepository.sumTotalPayroll();
    return response;
  }
}
