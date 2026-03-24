package com.example.hr.service;

import com.example.hr.dto.DashboardResponse;
import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.model.Salary;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.SalaryRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Dashboard service builds management-level charts and KPIs.
 */
@Service
public class DashboardService {
  private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM", Locale.CHINA);

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

    buildTrends(response, employees);
    buildDepartmentDistribution(response, employees);
    buildAttendanceIssues(response, attendances);
    buildPayrollByDepartment(response, salaries);

    return response;
  }

  private void buildTrends(DashboardResponse response, List<Employee> employees) {
    List<YearMonth> months = new ArrayList<>();
    YearMonth current = YearMonth.now();
    for (int i = 5; i >= 0; i--) {
      months.add(current.minusMonths(i));
    }

    Map<YearMonth, Long> hires = new LinkedHashMap<>();
    Map<YearMonth, Long> leaves = new LinkedHashMap<>();
    for (YearMonth month : months) {
      hires.put(month, 0L);
      leaves.put(month, 0L);
    }

    for (Employee e : employees) {
      if (e.getHireDate() != null) {
        YearMonth hiredMonth = YearMonth.from(e.getHireDate());
        if (hires.containsKey(hiredMonth)) {
          hires.put(hiredMonth, hires.get(hiredMonth) + 1);
        }
        // We don't have an exit date, so we approximate leaving by inactive status in the hire month.
        if (!"在职".equals(e.getStatus()) && leaves.containsKey(hiredMonth)) {
          leaves.put(hiredMonth, leaves.get(hiredMonth) + 1);
        }
      }
    }

    for (YearMonth month : months) {
      long headcount =
          employees.stream()
              .filter(
                  e -> e.getHireDate() == null || !e.getHireDate().isAfter(month.atEndOfMonth()))
              .count();
      response.headcountTrend.add(
          new DashboardResponse.MonthlyValue(month.format(MONTH_FORMATTER), headcount));
      response.flowTrend.add(
          new DashboardResponse.MonthlyFlow(
              month.format(MONTH_FORMATTER), hires.get(month), leaves.get(month)));
    }
  }

  private void buildDepartmentDistribution(DashboardResponse response, List<Employee> employees) {
    Map<String, Long> grouped =
        employees.stream()
            .collect(
                Collectors.groupingBy(
                    e -> departmentName(e),
                    Collectors.counting()));

    grouped.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .forEach(
            entry ->
                response.departmentDistribution.add(
                    new DashboardResponse.NamedValue(entry.getKey(), entry.getValue())));
  }

  private void buildAttendanceIssues(DashboardResponse response, List<Attendance> attendances) {
    LocalDate since = LocalDate.now().minusDays(30);
    Map<String, Long> grouped =
        attendances.stream()
            .filter(a -> a.getStatus() != null && !"Normal".equalsIgnoreCase(a.getStatus()))
            .filter(a -> a.getWorkDate() == null || !a.getWorkDate().isBefore(since))
            .collect(
                Collectors.groupingBy(
                    a -> a.getEmployee() != null ? a.getEmployee().getName() : "未知员工",
                    Collectors.counting()));

    grouped.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(8)
        .forEach(
            entry ->
                response.attendanceIssues.add(
                    new DashboardResponse.NamedValue(entry.getKey(), entry.getValue())));
  }

  private void buildPayrollByDepartment(DashboardResponse response, List<Salary> salaries) {
    Map<String, Double> grouped = new LinkedHashMap<>();
    for (Salary s : salaries) {
      String dept =
          s.getEmployee() != null ? departmentName(s.getEmployee()) : "未分配";
      double total =
          safeDouble(s.getBaseSalary()) + safeDouble(s.getBonus()) - safeDouble(s.getDeduction());
      grouped.put(dept, grouped.getOrDefault(dept, 0.0) + total);
    }

    grouped.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
        .limit(8)
        .forEach(
            entry ->
                response.payrollByDepartment.add(
                    new DashboardResponse.NamedValue(entry.getKey(), entry.getValue())));
  }

  private String departmentName(Employee employee) {
    if (employee == null) {
      return "未分配";
    }
    if (employee.getDepartmentRef() != null
        && employee.getDepartmentRef().getName() != null) {
      return employee.getDepartmentRef().getName();
    }
    if (employee.getDepartment() != null && !employee.getDepartment().isBlank()) {
      return employee.getDepartment();
    }
    return "未分配";
  }

  private double safeDouble(Double value) {
    return Objects.requireNonNullElse(value, 0.0);
  }
}
