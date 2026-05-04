package com.example.hr.service;

import com.example.hr.dto.DashboardResponse;
import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.model.Salary;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.SalaryRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

  private final EmployeeRepository employeeRepository;
  private final AttendanceRepository attendanceRepository;
  private final SalaryRepository salaryRepository;

  public DashboardService(
      EmployeeRepository employeeRepository,
      AttendanceRepository attendanceRepository,
      SalaryRepository salaryRepository) {
    this.employeeRepository = employeeRepository;
    this.attendanceRepository = attendanceRepository;
    this.salaryRepository = salaryRepository;
  }

  public DashboardResponse summary() {
    DashboardResponse response = new DashboardResponse();

    List<Employee> employees = employeeRepository.findAll();
    List<Attendance> attendances = attendanceRepository.findAll();
    List<Salary> salaries = salaryRepository.findAll();

    response.totalEmployees = employees.size();
    response.activeEmployees =
        employees.stream().filter(e -> "在职".equals(e.getStatus())).count();
    response.attendanceToday = attendanceRepository.countByWorkDate(LocalDate.now());
    response.totalPayroll = safeDouble(salaryRepository.sumTotalPayroll());

    buildStatusDistribution(response, employees);
    buildDepartmentDistribution(response, employees);
    buildAttendanceIssues(response, attendances);
    buildPayrollByDepartment(response, salaries);

    return response;
  }

  private void buildStatusDistribution(DashboardResponse response, List<Employee> employees) {
    Map<String, Long> grouped = employees.stream()
        .collect(Collectors.groupingBy(e -> e.getStatus(), Collectors.counting()));
    grouped.forEach((k, v) ->
        response.statusDistribution.add(new DashboardResponse.NamedValue(k, v)));
  }

  private void buildDepartmentDistribution(DashboardResponse response, List<Employee> employees) {
    Map<String, Long> grouped = employees.stream()
        .filter(e -> "在职".equals(e.getStatus()))
        .collect(Collectors.groupingBy(this::departmentName, Collectors.counting()));

    grouped.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .forEach(e ->
            response.departmentDistribution.add(
                new DashboardResponse.NamedValue(e.getKey(), e.getValue())));
  }

  private void buildAttendanceIssues(DashboardResponse response, List<Attendance> attendances) {
    LocalDate since = LocalDate.now().minusDays(30);
    Map<String, Long> grouped = attendances.stream()
        .filter(a -> a.getStatus() != null && !"Normal".equalsIgnoreCase(a.getStatus()))
        .filter(a -> a.getWorkDate() == null || !a.getWorkDate().isBefore(since))
        .collect(Collectors.groupingBy(
            a -> a.getEmployee() != null ? a.getEmployee().getName() : "未知",
            Collectors.counting()));

    grouped.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(8)
        .forEach(e ->
            response.attendanceIssues.add(
                new DashboardResponse.NamedValue(e.getKey(), e.getValue())));
  }

  private void buildPayrollByDepartment(DashboardResponse response, List<Salary> salaries) {
    Map<String, Double> grouped = new LinkedHashMap<>();
    for (Salary s : salaries) {
      String dept = s.getEmployee() != null ? departmentName(s.getEmployee()) : "未分配";
      double total = safeDouble(s.getBaseSalary()) + safeDouble(s.getBonus()) - safeDouble(s.getDeduction());
      grouped.put(dept, grouped.getOrDefault(dept, 0.0) + total);
    }

    grouped.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
        .limit(8)
        .forEach(e ->
            response.payrollByDepartment.add(
                new DashboardResponse.NamedValue(e.getKey(), e.getValue())));
  }

  private String departmentName(Employee employee) {
    if (employee == null) return "未分配";
    if (employee.getOrgRef() != null && employee.getOrgRef().getName() != null) {
      return employee.getOrgRef().getName();
    }
    return "未分配";
  }

  private double safeDouble(Double value) {
    return Objects.requireNonNullElse(value, 0.0);
  }
}
