package com.example.hr.service;

import com.example.hr.dto.SalaryRequest;
import com.example.hr.model.Employee;
import com.example.hr.model.Salary;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.SalaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Salary service for CRUD operations.
 */
@Service
public class SalaryService {
  private final SalaryRepository salaryRepository;
  private final EmployeeRepository employeeRepository;

  public SalaryService(SalaryRepository salaryRepository, EmployeeRepository employeeRepository) {
    this.salaryRepository = salaryRepository;
    this.employeeRepository = employeeRepository;
  }

  public List<Salary> list() {
    return salaryRepository.findAll();
  }

  public Salary create(SalaryRequest request) {
    Salary salary = new Salary();
    apply(salary, request);
    return salaryRepository.save(salary);
  }

  public Salary update(Long id, SalaryRequest request) {
    Salary salary =
        salaryRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Salary not found"));
    apply(salary, request);
    return salaryRepository.save(salary);
  }

  public void delete(Long id) {
    salaryRepository.deleteById(id);
  }

  private void apply(Salary salary, SalaryRequest request) {
    Employee employee =
        employeeRepository
            .findById(request.employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    validateAmounts(request.baseSalary, request.bonus, request.deduction);
    salary.setEmployee(employee);
    salary.setSalaryMonth(request.salaryMonth);
    salary.setBaseSalary(request.baseSalary);
    salary.setBonus(request.bonus);
    salary.setDeduction(request.deduction);
    salary.setNote(request.note);
  }

  private void validateAmounts(Double base, Double bonus, Double deduction) {
    if (base == null || bonus == null || deduction == null) {
      throw new IllegalArgumentException("Salary amounts are required");
    }
    double total = base + bonus - deduction;
    if (total < 0) {
      throw new IllegalArgumentException("Salary total cannot be negative");
    }
  }
}
