package com.example.hr.service;

import com.example.hr.dto.EmployeeRequest;
import com.example.hr.model.Employee;
import com.example.hr.repository.EmployeeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Employee service with CRUD business logic.
 */
@Service
public class EmployeeService {
  private final EmployeeRepository employeeRepository;

  public EmployeeService(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  public List<Employee> list() {
    return employeeRepository.findAll();
  }

  public Employee create(EmployeeRequest request) {
    Employee employee = new Employee();
    apply(employee, request);
    return employeeRepository.save(employee);
  }

  public Employee update(Long id, EmployeeRequest request) {
    Employee employee =
        employeeRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    apply(employee, request);
    return employeeRepository.save(employee);
  }

  public void delete(Long id) {
    employeeRepository.deleteById(id);
  }

  private void apply(Employee employee, EmployeeRequest request) {
    employee.setEmployeeNo(request.employeeNo);
    employee.setName(request.name);
    employee.setDepartment(request.department);
    employee.setTitle(request.title);
    employee.setPhone(request.phone);
    employee.setEmail(request.email);
    employee.setHireDate(request.hireDate);
    employee.setStatus(request.status);
  }
}
