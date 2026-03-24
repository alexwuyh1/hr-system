package com.example.hr.controller;

import com.example.hr.dto.EmployeeRequest;
import com.example.hr.dto.EmployeeResponse;
import com.example.hr.dto.ResignRequest;
import com.example.hr.dto.RehireRequest;
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
  public List<EmployeeResponse> list() {
    return employeeService.list().stream().map(EmployeeResponse::from).toList();
  }

  @PostMapping
  public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
    return EmployeeResponse.from(employeeService.create(request));
  }

  @PutMapping("/{id}")
  public EmployeeResponse update(@PathVariable("id") Long id, @Valid @RequestBody EmployeeRequest request) {
    return EmployeeResponse.from(employeeService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable("id") Long id) {
    employeeService.delete(id);
  }

  @PostMapping("/resign")
  public EmployeeResponse resign(@Valid @RequestBody ResignRequest request) {
    return EmployeeResponse.from(employeeService.resign(request.employeeNo));
  }

  @PostMapping("/rehire")
  public EmployeeResponse rehire(@Valid @RequestBody RehireRequest request) {
    return EmployeeResponse.from(employeeService.rehire(request.employeeNo));
  }
}
