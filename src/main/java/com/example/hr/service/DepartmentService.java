package com.example.hr.service;

import com.example.hr.model.Department;
import com.example.hr.repository.DepartmentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Department service for CRUD and tree building.
 */
@Service
public class DepartmentService {
  private final DepartmentRepository departmentRepository;

  public DepartmentService(DepartmentRepository departmentRepository) {
    this.departmentRepository = departmentRepository;
  }

  public List<Department> list() {
    return departmentRepository.findAll();
  }

  public Department create(Department department) {
    return departmentRepository.save(department);
  }

  public Department update(Long id, Department department) {
    Department existing =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    existing.setName(department.getName());
    existing.setParent(department.getParent());
    return departmentRepository.save(existing);
  }

  public void delete(Long id) {
    departmentRepository.deleteById(id);
  }

  public List<DepartmentNode> tree() {
    List<Department> all = departmentRepository.findAll();
    Map<Long, DepartmentNode> map = new HashMap<>();
    List<DepartmentNode> roots = new ArrayList<>();
    for (Department d : all) {
      map.put(d.getId(), new DepartmentNode(d.getId(), d.getName(), d.getParent() == null ? null : d.getParent().getId()));
    }
    for (DepartmentNode node : map.values()) {
      if (node.parentId == null) {
        roots.add(node);
      } else {
        DepartmentNode parent = map.get(node.parentId);
        if (parent != null) {
          parent.children.add(node);
        } else {
          roots.add(node);
        }
      }
    }
    return roots;
  }

  public static class DepartmentNode {
    public Long id;
    public String name;
    public Long parentId;
    public List<DepartmentNode> children = new ArrayList<>();

    public DepartmentNode(Long id, String name, Long parentId) {
      this.id = id;
      this.name = name;
      this.parentId = parentId;
    }
  }
}
