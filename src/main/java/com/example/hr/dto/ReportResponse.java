package com.example.hr.dto;

/**
 * Simple report response for dashboard numbers.
 * Keep it small and JSON friendly for the frontend.
 */
public class ReportResponse {
  public long totalEmployees;
  public long activeEmployees;
  public long attendanceToday;
  public double totalPayroll;
}
