package com.example.hr.model;

import jakarta.persistence.*;

/**
 * User entity for authentication and authorization.
 * Uses username + password hash and a simple role string.
 */
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  // Unique login name
  @Column(nullable = false, unique = true)
  private String username;

  // BCrypt hashed password
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  // Simple role: 管理员 / 员工
  @Column(nullable = false)
  private String role;

  // Record creation timestamp (epoch millis for SQLite compatibility)
  @Column(name = "created_at", nullable = false)
  private Long createdAt;

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }
}
