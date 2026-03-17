package com.example.hr.repository;

import com.example.hr.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Employee repository for CRUD operations.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
