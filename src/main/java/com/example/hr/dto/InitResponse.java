package com.example.hr.dto;

import com.example.hr.model.AttendanceRule;
import com.example.hr.model.Organization;
import com.example.hr.model.Permission;
import java.util.ArrayList;
import java.util.List;

public class InitResponse {
  public DashboardResponse dashboard;
  public AttendanceRule attendanceRule;
  public List<Organization> organizations = new ArrayList<>();
  public List<String> roles = new ArrayList<>();
  public List<Permission> permissions = new ArrayList<>();
}
