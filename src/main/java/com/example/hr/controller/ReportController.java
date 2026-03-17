package com.example.hr.controller;

import com.example.hr.dto.ReportResponse;
import com.example.hr.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Report endpoint for aggregated KPIs.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @GetMapping("/summary")
  public ReportResponse summary() {
    return reportService.summary();
  }
}
