package com.example.hr.controller;

import com.example.hr.dto.SalaryRequest;
import com.example.hr.model.Salary;
import com.example.hr.service.SalaryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Salary CRUD endpoints.
 */
@RestController
@RequestMapping("/api/salaries")
public class SalaryController {
  private final SalaryService salaryService;

  public SalaryController(SalaryService salaryService) {
    this.salaryService = salaryService;
  }

  @GetMapping
  public List<Salary> list() {
    return salaryService.list();
  }

  @PostMapping
  public Salary create(@Valid @RequestBody SalaryRequest request) {
    return salaryService.create(request);
  }

  @PutMapping("/{id}")
  public Salary update(@PathVariable Long id, @Valid @RequestBody SalaryRequest request) {
    return salaryService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    salaryService.delete(id);
  }
}
