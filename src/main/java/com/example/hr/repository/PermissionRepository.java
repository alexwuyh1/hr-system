package com.example.hr.repository;

import com.example.hr.model.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Permission repository for role-based access control rules.
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {
  List<Permission> findByRole(String role);
  List<Permission> findByRoleAndMethodAndMode(String role, String method, String mode);
  boolean existsByRoleAndMethodAndPathPrefixAndMode(String role, String method, String pathPrefix, String mode);
  @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.role FROM Permission p")
  java.util.List<String> findDistinctRoles();
}
