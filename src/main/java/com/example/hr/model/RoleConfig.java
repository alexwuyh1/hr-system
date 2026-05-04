package com.example.hr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "role_config")
public class RoleConfig {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @Column(nullable = false, unique = true)
  private String role;

  @Column(name = "role_mode", nullable = false)
  private String roleMode;

  public Long getId() {
    return id;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getRoleMode() {
    return roleMode;
  }

  public void setRoleMode(String roleMode) {
    this.roleMode = roleMode;
  }
}
