package com.example.hr.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard response for management-level visualization.
 * Carries KPI cards plus chart-ready series data.
 */
public class DashboardResponse {
  // KPI cards
  public long totalEmployees;
  public long activeEmployees;
  public long attendanceToday;
  public double totalPayroll;

  // Chart series
  public List<MonthlyValue> headcountTrend = new ArrayList<>();
  public List<MonthlyFlow> flowTrend = new ArrayList<>();
  public List<NamedValue> departmentDistribution = new ArrayList<>();
  public List<NamedValue> attendanceIssues = new ArrayList<>();
  public List<NamedValue> payrollByDepartment = new ArrayList<>();

  /**
   * Month-value pair for trend charts.
   */
  public static class MonthlyValue {
    public String month;
    public long value;

    public MonthlyValue() {}

    public MonthlyValue(String month, long value) {
      this.month = month;
      this.value = value;
    }
  }

  /**
   * Monthly flow data for hiring vs leaving charts.
   */
  public static class MonthlyFlow {
    public String month;
    public long hired;
    public long left;

    public MonthlyFlow() {}

    public MonthlyFlow(String month, long hired, long left) {
      this.month = month;
      this.hired = hired;
      this.left = left;
    }
  }

  /**
   * Name-value pair for distribution charts.
   */
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
