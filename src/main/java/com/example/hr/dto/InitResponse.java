package com.example.hr.dto;

import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Department;
import com.example.hr.model.Grade;
import com.example.hr.model.Permission;
import com.example.hr.model.Position;
import com.example.hr.model.Role;
import com.example.hr.service.DepartmentService;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated init payload for client bootstrap.
 */
public class InitResponse {
  public DashboardResponse dashboard;
  public AttendanceRule attendanceRule;
  public List<Department> departments = new ArrayList<>();
  public List<DepartmentService.DepartmentNode> departmentTree = new ArrayList<>();
  public List<Position> positions = new ArrayList<>();
  public List<Grade> grades = new ArrayList<>();
  public List<Role> roles = new ArrayList<>();
  public List<Permission> permissions = new ArrayList<>();
}
