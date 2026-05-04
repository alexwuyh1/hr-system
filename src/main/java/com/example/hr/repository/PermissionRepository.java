package com.example.hr.repository;

import com.example.hr.model.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  List<Permission> findByRole(String role);
  List<Permission> findByRoleAndMethod(String role, String method);
  boolean existsByRoleAndMethodAndPathPrefix(String role, String method, String pathPrefix);
  @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.role FROM Permission p")
  java.util.List<String> findDistinctRoles();
}
