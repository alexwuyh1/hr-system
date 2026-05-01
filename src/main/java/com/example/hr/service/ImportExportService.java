package com.example.hr.service;

import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.model.Salary;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.SalaryRepository;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Import/Export service for CSV and Excel.
 * Supports Employees, Attendance, Salaries.
 */
@Service
public class ImportExportService {
  private final EmployeeRepository employeeRepository;
  private final AttendanceRepository attendanceRepository;
  private final SalaryRepository salaryRepository;

  public ImportExportService(
      EmployeeRepository employeeRepository,
      AttendanceRepository attendanceRepository,
      SalaryRepository salaryRepository) {
    this.employeeRepository = employeeRepository;
    this.attendanceRepository = attendanceRepository;
    this.salaryRepository = salaryRepository;
  }

  public int importEmployees(MultipartFile file, String format) throws Exception {
    if ("xlsx".equalsIgnoreCase(format)) {
      return importEmployeesExcel(file.getInputStream());
    }
    return importEmployeesCsv(file);
  }

  public int importAttendance(MultipartFile file, String format) throws Exception {
    if ("xlsx".equalsIgnoreCase(format)) {
      return importAttendanceExcel(file.getInputStream());
    }
    return importAttendanceCsv(file);
  }

  public int importSalaries(MultipartFile file, String format) throws Exception {
    if ("xlsx".equalsIgnoreCase(format)) {
      return importSalariesExcel(file.getInputStream());
    }
    return importSalariesCsv(file);
  }

  public byte[] exportEmployeesCsv() {
    StringBuilder sb = new StringBuilder();
    sb.append("employeeNo,name,department,title,phone,email,hireDate,status\n");
    for (Employee e : employeeRepository.findAll()) {
      sb.append(e.getEmployeeNo()).append(",")
          .append(safe(e.getName())).append(",")
          .append(safe(e.getDepartment())).append(",")
          .append(safe(e.getTitle())).append(",")
          .append(safe(e.getPhone())).append(",")
          .append(safe(e.getEmail())).append(",")
          .append(e.getHireDate()).append(",")
          .append(safe(e.getStatus())).append("\n");
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public byte[] exportAttendanceCsv() {
    StringBuilder sb = new StringBuilder();
    sb.append("employeeId,workDate,checkIn,checkOut,status,note\n");
    for (Attendance a : attendanceRepository.findAll()) {
      sb.append(a.getEmployee().getId()).append(",")
          .append(a.getWorkDate()).append(",")
          .append(a.getCheckIn() == null ? "" : a.getCheckIn()).append(",")
          .append(a.getCheckOut() == null ? "" : a.getCheckOut()).append(",")
          .append(safe(a.getStatus())).append(",")
          .append(safe(a.getNote())).append("\n");
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public byte[] exportSalariesCsv() {
    StringBuilder sb = new StringBuilder();
    sb.append("employeeId,salaryMonth,baseSalary,bonus,deduction,note\n");
    for (Salary s : salaryRepository.findAll()) {
      sb.append(s.getEmployee().getId()).append(",")
          .append(s.getSalaryMonth()).append(",")
          .append(s.getBaseSalary()).append(",")
          .append(s.getBonus()).append(",")
          .append(s.getDeduction()).append(",")
          .append(safe(s.getNote())).append("\n");
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public byte[] exportEmployeesExcel() throws Exception {
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("employees");
      Row header = sheet.createRow(0);
      String[] headers = {"employeeNo","name","department","title","phone","email","hireDate","status"};
      for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
      int row = 1;
      for (Employee e : employeeRepository.findAll()) {
        Row r = sheet.createRow(row++);
        r.createCell(0).setCellValue(e.getEmployeeNo());
        r.createCell(1).setCellValue(e.getName());
        r.createCell(2).setCellValue(e.getDepartment());
        r.createCell(3).setCellValue(e.getTitle());
        r.createCell(4).setCellValue(nullSafe(e.getPhone()));
        r.createCell(5).setCellValue(nullSafe(e.getEmail()));
        r.createCell(6).setCellValue(e.getHireDate().toString());
        r.createCell(7).setCellValue(e.getStatus());
      }
      return workbookToBytes(wb);
    }
  }

  public byte[] exportAttendanceExcel() throws Exception {
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("attendance");
      Row header = sheet.createRow(0);
      String[] headers = {"employeeId","workDate","checkIn","checkOut","status","note"};
      for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
      int row = 1;
      for (Attendance a : attendanceRepository.findAll()) {
        Row r = sheet.createRow(row++);
        r.createCell(0).setCellValue(a.getEmployee().getId());
        r.createCell(1).setCellValue(a.getWorkDate().toString());
        r.createCell(2).setCellValue(a.getCheckIn() == null ? "" : a.getCheckIn().toString());
        r.createCell(3).setCellValue(a.getCheckOut() == null ? "" : a.getCheckOut().toString());
        r.createCell(4).setCellValue(a.getStatus());
        r.createCell(5).setCellValue(nullSafe(a.getNote()));
      }
      return workbookToBytes(wb);
    }
  }

  public byte[] exportSalariesExcel() throws Exception {
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("salaries");
      Row header = sheet.createRow(0);
      String[] headers = {"employeeId","salaryMonth","baseSalary","bonus","deduction","note"};
      for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
      int row = 1;
      for (Salary s : salaryRepository.findAll()) {
        Row r = sheet.createRow(row++);
        r.createCell(0).setCellValue(s.getEmployee().getId());
        r.createCell(1).setCellValue(s.getSalaryMonth());
        r.createCell(2).setCellValue(s.getBaseSalary());
        r.createCell(3).setCellValue(s.getBonus());
        r.createCell(4).setCellValue(s.getDeduction());
        r.createCell(5).setCellValue(nullSafe(s.getNote()));
      }
      return workbookToBytes(wb);
    }
  }

  private int importEmployeesCsv(MultipartFile file) throws Exception {
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    String[] lines = content.split("\\r?\\n");
    int count = 0;
    for (int i = 1; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty()) continue;
      String[] parts = line.split(",", -1);
      String employeeNo = parts[0];
      
      // 检查是否已存在，存在则更新，否则插入
      Employee e = employeeRepository.findByEmployeeNo(employeeNo).orElse(null);
      if (e == null) {
        e = new Employee();
        e.setEmployeeNo(employeeNo);
      }
      e.setName(parts[1]);
      e.setDepartment(parts[2]);
      e.setTitle(parts[3]);
      e.setPhone(parts[4].isEmpty() ? null : parts[4]);
      e.setEmail(parts[5].isEmpty() ? null : parts[5]);
      e.setHireDate(LocalDate.parse(parts[6]));
      e.setStatus(parts[7]);
      employeeRepository.save(e);
      count++;
    }
    return count;
  }

  private int importAttendanceCsv(MultipartFile file) throws Exception {
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    String[] lines = content.split("\\r?\\n");
    int count = 0;
    for (int i = 1; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty()) continue;
      String[] parts = line.split(",", -1);
      Attendance a = new Attendance();
      a.setEmployee(employeeRepository.findById(Long.parseLong(parts[0]))
          .orElseThrow(() -> new IllegalArgumentException("Employee not found")));
      a.setWorkDate(LocalDate.parse(parts[1]));
      a.setCheckIn(parts[2].isEmpty() ? null : LocalTime.parse(parts[2]));
      a.setCheckOut(parts[3].isEmpty() ? null : LocalTime.parse(parts[3]));
      a.setStatus(parts[4]);
      a.setNote(parts[5].isEmpty() ? null : parts[5]);
      attendanceRepository.save(a);
      count++;
    }
    return count;
  }

  private int importSalariesCsv(MultipartFile file) throws Exception {
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    String[] lines = content.split("\\r?\\n");
    int count = 0;
    for (int i = 1; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty()) continue;
      String[] parts = line.split(",", -1);
      
      Employee employee = employeeRepository.findById(Long.parseLong(parts[0]))
          .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
      String salaryMonth = parts[1];
      
      // 检查是否已存在，存在则更新，否则插入
      Salary s = salaryRepository.findByEmployeeAndSalaryMonth(employee, salaryMonth);
      if (s == null) {
        s = new Salary();
        s.setEmployee(employee);
        s.setSalaryMonth(salaryMonth);
      }
      s.setBaseSalary(Double.parseDouble(parts[2]));
      s.setBonus(Double.parseDouble(parts[3]));
      s.setDeduction(Double.parseDouble(parts[4]));
      s.setNote(parts[5].isEmpty() ? null : parts[5]);
      salaryRepository.save(s);
      count++;
    }
    return count;
  }

  private int importEmployeesExcel(InputStream inputStream) throws Exception {
    try (Workbook wb = new XSSFWorkbook(inputStream)) {
      Sheet sheet = wb.getSheetAt(0);
      int count = 0;
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row r = sheet.getRow(i);
        if (r == null) continue;
        String employeeNo = getString(r, 0);
        
        // 检查是否已存在，存在则更新，否则插入
        Employee e = employeeRepository.findByEmployeeNo(employeeNo).orElse(null);
        if (e == null) {
          e = new Employee();
          e.setEmployeeNo(employeeNo);
        }
        e.setName(getString(r, 1));
        e.setDepartment(getString(r, 2));
        e.setTitle(getString(r, 3));
        e.setPhone(getString(r, 4));
        e.setEmail(getString(r, 5));
        e.setHireDate(LocalDate.parse(getString(r, 6)));
        e.setStatus(getString(r, 7));
        employeeRepository.save(e);
        count++;
      }
      return count;
    }
  }

  private int importAttendanceExcel(InputStream inputStream) throws Exception {
    try (Workbook wb = new XSSFWorkbook(inputStream)) {
      Sheet sheet = wb.getSheetAt(0);
      int count = 0;
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row r = sheet.getRow(i);
        if (r == null) continue;
        Attendance a = new Attendance();
        a.setEmployee(employeeRepository.findById(Long.parseLong(getString(r, 0)))
            .orElseThrow(() -> new IllegalArgumentException("Employee not found")));
        a.setWorkDate(LocalDate.parse(getString(r, 1)));
        a.setCheckIn(parseTime(getString(r, 2)));
        a.setCheckOut(parseTime(getString(r, 3)));
        a.setStatus(getString(r, 4));
        a.setNote(getString(r, 5));
        attendanceRepository.save(a);
        count++;
      }
      return count;
    }
  }

  private int importSalariesExcel(InputStream inputStream) throws Exception {
    try (Workbook wb = new XSSFWorkbook(inputStream)) {
      Sheet sheet = wb.getSheetAt(0);
      int count = 0;
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row r = sheet.getRow(i);
        if (r == null) continue;
        
        Employee employee = employeeRepository.findById(Long.parseLong(getString(r, 0)))
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        String salaryMonth = getString(r, 1);
        
        // 检查是否已存在，存在则更新，否则插入
        Salary s = salaryRepository.findByEmployeeAndSalaryMonth(employee, salaryMonth);
        if (s == null) {
          s = new Salary();
          s.setEmployee(employee);
          s.setSalaryMonth(salaryMonth);
        }
        s.setBaseSalary(Double.parseDouble(getString(r, 2)));
        s.setBonus(Double.parseDouble(getString(r, 3)));
        s.setDeduction(Double.parseDouble(getString(r, 4)));
        s.setNote(getString(r, 5));
        salaryRepository.save(s);
        count++;
      }
      return count;
    }
  }

  private String getString(Row r, int idx) {
    Cell cell = r.getCell(idx);
    if (cell == null) return "";
    
    // 根据单元格类型获取值
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue().trim();
      case NUMERIC:
        // 数字类型：如果是整数则去掉小数部分
        double value = cell.getNumericCellValue();
        if (value == (long) value) {
          return String.valueOf((long) value);
        }
        return String.valueOf(value);
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      default:
        return "";
    }
  }

  private LocalTime parseTime(String value) {
    if (value == null || value.isBlank()) return null;
    return LocalTime.parse(value.trim());
  }

  private byte[] workbookToBytes(Workbook wb) throws Exception {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      wb.write(out);
      return out.toByteArray();
    }
  }

  private String safe(String value) {
    return value == null ? "" : value.replace(",", " ");
  }

  private String nullSafe(String value) {
    return value == null ? "" : value;
  }
}
