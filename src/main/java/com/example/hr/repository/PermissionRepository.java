package com.example.hr.repository;

import com.example.hr.model.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Permission repository for role-based access control rules.
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {
  List<Permission> findByRoleAndMethod(String role, String method);
  boolean existsByRoleAndMethodAndPathPrefix(String role, String method, String pathPrefix);
}
