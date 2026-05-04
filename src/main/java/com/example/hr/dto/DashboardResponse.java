package com.example.hr.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardResponse {
  public long totalEmployees;
  public long activeEmployees;
  public long attendanceToday;
  public double totalPayroll;

  public List<NamedValue> statusDistribution = new ArrayList<>();
  public List<NamedValue> departmentDistribution = new ArrayList<>();
  public List<NamedValue> attendanceIssues = new ArrayList<>();
  public List<NamedValue> payrollByDepartment = new ArrayList<>();

  public static class NamedValue {
    public String name;
    public double value;

    public NamedValue() {}

    public NamedValue(String name, double value) {
      this.name = name;
      this.value = value;
    }
  }
}
