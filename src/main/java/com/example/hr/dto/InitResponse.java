package com.example.hr.dto;

import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Organization;
import com.example.hr.model.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InitResponse {
  public DashboardResponse dashboard;
  public AttendanceRule attendanceRule;
  public List<Organization> organizations = new ArrayList<>();
  public List<Map<String, String>> roles = new ArrayList<>();
  public List<Permission> permissions = new ArrayList<>();
}
