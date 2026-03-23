package com.example.hr.controller;

import com.example.hr.service.ImportExportService;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Import/Export endpoints for CSV and Excel.
 */
@RestController
@RequestMapping("/api/data")
public class ImportExportController {
  private final ImportExportService importExportService;

  public ImportExportController(ImportExportService importExportService) {
    this.importExportService = importExportService;
  }

  @PostMapping("/import/{type}")
  public Map<String, Integer> importData(
      @PathVariable("type") String type,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "format", defaultValue = "csv") String format) throws Exception {
    int count;
    switch (type) {
      case "employees" -> count = importExportService.importEmployees(file, format);
      case "attendance" -> count = importExportService.importAttendance(file, format);
      case "salaries" -> count = importExportService.importSalaries(file, format);
      default -> throw new IllegalArgumentException("Unknown import type");
    }
    return Map.of("imported", count);
  }

  @GetMapping("/export/{type}")
  public ResponseEntity<byte[]> exportData(
      @PathVariable("type") String type,
      @RequestParam(value = "format", defaultValue = "csv") String format) throws Exception {
    byte[] content;
    String filename;
    MediaType mediaType;
    if ("xlsx".equalsIgnoreCase(format)) {
      mediaType = MediaType.parseMediaType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      switch (type) {
        case "employees" -> content = importExportService.exportEmployeesExcel();
        case "attendance" -> content = importExportService.exportAttendanceExcel();
        case "salaries" -> content = importExportService.exportSalariesExcel();
        default -> throw new IllegalArgumentException("Unknown export type");
      }
      filename = type + ".xlsx";
    } else {
      mediaType = MediaType.TEXT_PLAIN;
      switch (type) {
        case "employees" -> content = importExportService.exportEmployeesCsv();
        case "attendance" -> content = importExportService.exportAttendanceCsv();
        case "salaries" -> content = importExportService.exportSalariesCsv();
        default -> throw new IllegalArgumentException("Unknown export type");
      }
      filename = type + ".csv";
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(mediaType)
        .body(content);
  }
}
