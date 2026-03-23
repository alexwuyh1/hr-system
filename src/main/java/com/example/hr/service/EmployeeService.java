package com.example.hr.service;

import com.example.hr.dto.EmployeeRequest;
import com.example.hr.model.Employee;
import com.example.hr.model.Department;
import com.example.hr.model.Position;
import com.example.hr.model.Grade;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.PositionRepository;
import com.example.hr.repository.GradeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Employee service with CRUD business logic.
 */
@Service
public class EmployeeService {
  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final GradeRepository gradeRepository;

  public EmployeeService(
      EmployeeRepository employeeRepository,
      DepartmentRepository departmentRepository,
      PositionRepository positionRepository,
      GradeRepository gradeRepository) {
    this.employeeRepository = employeeRepository;
    this.departmentRepository = departmentRepository;
    this.positionRepository = positionRepository;
    this.gradeRepository = gradeRepository;
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

    if (request.departmentId != null) {
      Department department =
          departmentRepository
              .findById(request.departmentId)
              .orElseThrow(() -> new IllegalArgumentException("Department not found"));
      employee.setDepartmentRef(department);
      // Keep legacy text field in sync
      employee.setDepartment(department.getName());
    }

    if (request.positionId != null) {
      Position position =
          positionRepository
              .findById(request.positionId)
              .orElseThrow(() -> new IllegalArgumentException("Position not found"));
      employee.setPositionRef(position);
      employee.setTitle(position.getName());
    }

    if (request.gradeId != null) {
      Grade grade =
          gradeRepository
              .findById(request.gradeId)
              .orElseThrow(() -> new IllegalArgumentException("Grade not found"));
      employee.setGradeRef(grade);
    }

    if (request.managerId != null) {
      Employee manager =
          employeeRepository
              .findById(request.managerId)
              .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
      employee.setManagerRef(manager);
    } else {
      employee.setManagerRef(null);
    }
  }
}
