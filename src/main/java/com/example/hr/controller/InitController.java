package com.example.hr.controller;

import com.example.hr.dto.InitResponse;
import com.example.hr.repository.GradeRepository;
import com.example.hr.repository.PermissionRepository;
import com.example.hr.repository.PositionRepository;
import com.example.hr.repository.RoleRepository;
import com.example.hr.service.AttendanceRuleService;
import com.example.hr.service.DashboardService;
import com.example.hr.service.DepartmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aggregated init endpoint to reduce frontend request fan-out.
 */
@RestController
@RequestMapping("/api/init")
public class InitController {
  private final DashboardService dashboardService;
  private final AttendanceRuleService attendanceRuleService;
  private final DepartmentService departmentService;
  private final PositionRepository positionRepository;
  private final GradeRepository gradeRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  public InitController(
      DashboardService dashboardService,
      AttendanceRuleService attendanceRuleService,
      DepartmentService departmentService,
      PositionRepository positionRepository,
      GradeRepository gradeRepository,
      RoleRepository roleRepository,
      PermissionRepository permissionRepository) {
    this.dashboardService = dashboardService;
    this.attendanceRuleService = attendanceRuleService;
    this.departmentService = departmentService;
    this.positionRepository = positionRepository;
    this.gradeRepository = gradeRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
  }

  @GetMapping
  public InitResponse init() {
    InitResponse response = new InitResponse();
    response.dashboard = dashboardService.summary();
    response.attendanceRule = attendanceRuleService.getRule();
    response.departments = departmentService.list();
    response.departmentTree = departmentService.tree();
    response.positions = positionRepository.findAll();
    response.grades = gradeRepository.findAll();
    response.roles = roleRepository.findAll();
    response.permissions = permissionRepository.findAll();
    return response;
  }
}
