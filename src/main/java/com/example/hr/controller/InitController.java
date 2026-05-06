package com.example.hr.controller;

import com.example.hr.dto.InitResponse;
import com.example.hr.service.AttendanceRuleService;
import com.example.hr.service.DashboardService;
import com.example.hr.service.OrganizationService;
import com.example.hr.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/init")
public class InitController {
  private final DashboardService dashboardService;
  private final AttendanceRuleService attendanceRuleService;
  private final OrganizationService organizationService;
  private final PermissionService permissionService;

  public InitController(
      DashboardService dashboardService,
      AttendanceRuleService attendanceRuleService,
      OrganizationService organizationService,
      PermissionService permissionService) {
    this.dashboardService = dashboardService;
    this.attendanceRuleService = attendanceRuleService;
    this.organizationService = organizationService;
    this.permissionService = permissionService;
  }

  @GetMapping
  public InitResponse init() {
    InitResponse response = new InitResponse();
    response.dashboard = dashboardService.summary();
    response.attendanceRule = attendanceRuleService.getRule();
    response.organizations = organizationService.list();
    response.roles = permissionService.listRolesWithMode();
    response.permissions = permissionService.list();
    return response;
  }
}
