package com.example.hr.repository;

import com.example.hr.model.Department;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Department repository.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
  List<Department> findByParentId(Long parentId);
}
