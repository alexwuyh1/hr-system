package com.example.hr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @Column(nullable = false)
  private String role;

  @Column(nullable = false)
  private String method;

  @Column(name = "path_prefix", nullable = false)
  private String pathPrefix;

  public Long getId() {
    return id;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getPathPrefix() {
    return pathPrefix;
  }

  public void setPathPrefix(String pathPrefix) {
    this.pathPrefix = pathPrefix;
  }
}
