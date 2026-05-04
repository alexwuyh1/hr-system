package com.example.hr.repository;

import com.example.hr.model.Employee;
import com.example.hr.model.Salary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Salary repository with a small summary query.
 */
public interface SalaryRepository extends JpaRepository<Salary, Long> {
  List<Salary> findBySalaryMonth(String salaryMonth);
  
  Salary findByEmployeeAndSalaryMonth(Employee employee, String salaryMonth);

  @Query("select coalesce(sum(s.baseSalary + s.bonus - s.deduction), 0) from Salary s")
  Double sumTotalPayroll();

  @Modifying
  void deleteByEmployee(Employee employee);
}
