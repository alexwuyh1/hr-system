package com.example.hr.controller;

import com.example.hr.model.Department;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.service.DepartmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * Department management endpoints.
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
  private final DepartmentService departmentService;
  private final DepartmentRepository departmentRepository;

  public DepartmentController(
      DepartmentService departmentService, DepartmentRepository departmentRepository) {
    this.departmentService = departmentService;
    this.departmentRepository = departmentRepository;
  }

  @GetMapping
  public List<Department> list() {
    return departmentService.list();
  }

  @GetMapping("/tree")
  public List<DepartmentService.DepartmentNode> tree() {
    return departmentService.tree();
  }

  @PostMapping
  public Department create(@Valid @RequestBody DepartmentRequest request) {
    Department department = new Department();
    department.setName(request.name);
    department.setParent(request.parentId == null ? null : loadParent(request.parentId));
    return departmentService.create(department);
  }

  @PutMapping("/{id}")
  public Department update(@PathVariable("id") Long id, @Valid @RequestBody DepartmentRequest request) {
    Department department = new Department();
    department.setName(request.name);
    department.setParent(request.parentId == null ? null : loadParent(request.parentId));
    return departmentService.update(id, department);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> delete(@PathVariable("id") Long id) {
    departmentService.delete(id);
    return Map.of("message", "Department deleted");
  }

  private Department loadParent(Long id) {
    return departmentRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Parent department not found"));
  }

  public static class DepartmentRequest {
    @NotBlank public String name;
    public Long parentId;
  }
}
