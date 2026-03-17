package com.example.hr.controller;

import com.example.hr.dto.EmployeeRequest;
import com.example.hr.model.Employee;
import com.example.hr.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Employee CRUD endpoints.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
  private final EmployeeService employeeService;

  public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @GetMapping
  public List<Employee> list() {
    return employeeService.list();
  }

  @PostMapping
  public Employee create(@Valid @RequestBody EmployeeRequest request) {
    return employeeService.create(request);
  }

  @PutMapping("/{id}")
  public Employee update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
    return employeeService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    employeeService.delete(id);
  }
}
