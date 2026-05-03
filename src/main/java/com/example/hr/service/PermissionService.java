package com.example.hr.service;

import com.example.hr.model.Permission;
import com.example.hr.repository.PermissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Permission service for CRUD and authorization checks.
 */
@Service
public class PermissionService {
  private final PermissionRepository permissionRepository;

  public PermissionService(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public List<Permission> list() {
    return permissionRepository.findAll();
  }

  public Permission create(Permission permission) {
    return permissionRepository.save(permission);
  }

  public Permission update(Long id, Permission permission) {
    Permission existing =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
    existing.setRole(permission.getRole());
    existing.setMethod(permission.getMethod());
    existing.setPathPrefix(permission.getPathPrefix());
    existing.setMode(permission.getMode());
    return permissionRepository.save(existing);
  }

  public void delete(Long id) {
    permissionRepository.deleteById(id);
  }

  public boolean isAllowed(String role, String method, String path) {
    if ("管理员".equals(role)) return true;
    
    List<Permission> denyRules = permissionRepository.findByRoleAndMethodAndMode(role, method, "deny");
    for (Permission p : denyRules) {
      if (path.startsWith(p.getPathPrefix())) return false;
    }
    
    List<Permission> allowRules = permissionRepository.findByRoleAndMethodAndMode(role, method, "allow");
    for (Permission p : allowRules) {
      if (path.startsWith(p.getPathPrefix())) return true;
    }
    
    return false;
  }
}
